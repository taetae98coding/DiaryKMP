@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.contact.ui.home

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.domain.contact.usecase.DeleteContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.PageContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.RestoreContactUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Instant
import kotlin.uuid.Uuid

class ContactHomeViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-CONTACT-HOME-FEATURE-001 조회한 연락처 페이지를 그대로 노출한다") {
            runTest(mainDispatcher) {
                val contactList = listOf(contact(), contact())
                val viewModel = viewModel(pageContactUseCase = pageContactUseCase(contactListFlow = flowOf(Result.success(contactList))))

                flowOf(viewModel.contactPagingData.first()).asSnapshot() shouldBe contactList
            }
        }

        test("TC-CONTACT-HOME-DATA-003 연락처 페이지 조회에 실패하면 빈 페이지를 노출한다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(
                        pageContactUseCase = pageContactUseCase(contactListFlow = flowOf(Result.failure(IllegalStateException()))),
                    )

                flowOf(viewModel.contactPagingData.first()).asSnapshot().shouldBeEmpty()
            }
        }

        test("TC-CONTACT-HOME-DOMAIN-006 저장된 연락처가 바뀌면 바뀐 페이지를 노출한다") {
            runTest(mainDispatcher) {
                val beforeContactList = listOf(contact())
                val afterContactList = listOf(contact(), contact())
                val contactListFlow = MutableStateFlow(Result.success(beforeContactList))
                val viewModel = viewModel(pageContactUseCase = pageContactUseCase(contactListFlow = contactListFlow))

                viewModel.contactPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe beforeContactList

                    contactListFlow.value = Result.success(afterContactList)

                    flowOf(awaitItem()).asSnapshot() shouldBe afterContactList
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-CONTACT-HOME-FEATURE-005 연락처가 새로 저장되면 별도 조작 없이 목록에 나타난다") {
            runTest(mainDispatcher) {
                val shownContactList = listOf(contact())
                val addedContact = contact()
                val contactListFlow = MutableStateFlow(Result.success(shownContactList))
                val viewModel = viewModel(pageContactUseCase = pageContactUseCase(contactListFlow = contactListFlow))

                viewModel.contactPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe shownContactList

                    contactListFlow.value = Result.success(shownContactList + addedContact)

                    flowOf(awaitItem()).asSnapshot() shouldContain addedContact
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("처음 정렬은 이름순이다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(pageContactUseCase = pageContactUseCase(contactListFlow = flowOf(Result.success(emptyList()))))

                viewModel.sort.value shouldBe ListSort.NAME
            }
        }

        test("TC-CONTACT-HOME-DATA-005 정렬을 바꾸면 그 정렬로 목록을 다시 조회한다") {
            runTest(mainDispatcher) {
                val nameContactList = listOf(contact(), contact())
                val recentlyUpdatedContactList = listOf(contact(), contact())
                val pageContactUseCase = mockk<PageContactUseCase>()
                every { pageContactUseCase(parameter = ListSort.NAME) } returns flowOf(Result.success(PagingData.from(nameContactList)))
                every { pageContactUseCase(parameter = ListSort.RECENTLY_UPDATED) } returns
                    flowOf(Result.success(PagingData.from(recentlyUpdatedContactList)))
                val viewModel = viewModel(pageContactUseCase = pageContactUseCase)

                viewModel.contactPagingData.test {
                    flowOf(awaitItem()).asSnapshot() shouldBe nameContactList

                    viewModel.select(sort = ListSort.RECENTLY_UPDATED)

                    flowOf(awaitItem()).asSnapshot() shouldBe recentlyUpdatedContactList
                    viewModel.sort.value shouldBe ListSort.RECENTLY_UPDATED
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-CONTACT-HOME-FEATURE-026 삭제에 성공하면 그 연락처의 삭제를 요청하고 삭제 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val deleteContactUseCase = mockk<DeleteContactUseCase>()
                coEvery { deleteContactUseCase(parameter = id) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        pageContactUseCase = pageContactUseCase(contactListFlow = flowOf(Result.success(emptyList()))),
                        deleteContactUseCase = deleteContactUseCase,
                    )

                viewModel.effect.test {
                    viewModel.delete(id = id)

                    awaitItem() shouldBe ContactHomeEffect.Deleted(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { deleteContactUseCase(parameter = id) }
            }
        }

        test("TC-CONTACT-HOME-FEATURE-032 삭제가 저장되지 못하면 삭제 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val deleteContactUseCase = mockk<DeleteContactUseCase>()
                coEvery { deleteContactUseCase(parameter = id) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel =
                    viewModel(
                        pageContactUseCase = pageContactUseCase(contactListFlow = flowOf(Result.success(emptyList()))),
                        deleteContactUseCase = deleteContactUseCase,
                    )

                viewModel.effect.test {
                    viewModel.delete(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-CONTACT-HOME-DOMAIN-015 실행 취소를 저장하지 못하면 별도 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val restoreContactUseCase = mockk<RestoreContactUseCase>()
                coEvery { restoreContactUseCase(parameter = id) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel =
                    viewModel(
                        pageContactUseCase = pageContactUseCase(contactListFlow = flowOf(Result.success(emptyList()))),
                        restoreContactUseCase = restoreContactUseCase,
                    )

                viewModel.effect.test {
                    viewModel.restore(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { restoreContactUseCase(parameter = id) }
            }
        }

        test("TC-CONTACT-HOME-FEATURE-027 실행 취소하면 그 연락처의 삭제를 되돌리는 요청을 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()
                val restoreContactUseCase = mockk<RestoreContactUseCase>()
                coEvery { restoreContactUseCase(parameter = id) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        pageContactUseCase = pageContactUseCase(contactListFlow = flowOf(Result.success(emptyList()))),
                        restoreContactUseCase = restoreContactUseCase,
                    )

                viewModel.restore(id = id)
                advanceUntilIdle()

                coVerify(exactly = 1) { restoreContactUseCase(parameter = id) }
            }
        }
    }

    private companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun viewModel(
            pageContactUseCase: PageContactUseCase,
            deleteContactUseCase: DeleteContactUseCase = mockk(),
            restoreContactUseCase: RestoreContactUseCase = mockk(),
        ): ContactHomeViewModel =
            ContactHomeViewModel(
                pageContactUseCase = pageContactUseCase,
                deleteContactUseCase = deleteContactUseCase,
                restoreContactUseCase = restoreContactUseCase,
            )

        private fun pageContactUseCase(contactListFlow: Flow<Result<List<Contact>>>): PageContactUseCase {
            val pageContactUseCase = mockk<PageContactUseCase>()
            every { pageContactUseCase(parameter = ListSort.NAME) } returns
                contactListFlow.map { result -> result.map { contactList -> PagingData.from(contactList) } }

            return pageContactUseCase
        }

        private fun contact(): Contact {
            val detail = fixtureMonkey.giveMeKotlinBuilder<ContactDetail>().sample()

            return Contact(
                id = Uuid.random(),
                detail = detail,
                isFavorite = false,
                isDeleted = false,
                updatedAt = fixtureMonkey.giveMeOne<Instant>(),
                createdAt = fixtureMonkey.giveMeOne<Instant>(),
            )
        }
    }
}
