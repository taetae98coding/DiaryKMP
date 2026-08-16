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
import io.github.taetae98coding.diary.domain.contact.usecase.PageContactUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
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
                val contactList = listOf(contact(name = "김철수"), contact(name = "이영희"))
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
                val beforeContactList = listOf(contact(name = "김철수"))
                val afterContactList = listOf(contact(name = "김철수"), contact(name = "이영희"))
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

        test("TC-CONTACT-HOME-DOMAIN-004 처음 정렬은 이름순이다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(pageContactUseCase = pageContactUseCase(contactListFlow = flowOf(Result.success(emptyList()))))

                viewModel.sort.value shouldBe ListSort.NAME
            }
        }

        test("TC-CONTACT-HOME-DATA-005 정렬을 바꾸면 그 정렬로 목록을 다시 조회한다") {
            runTest(mainDispatcher) {
                val nameContactList = listOf(contact(name = "김철수"), contact(name = "이영희"))
                val recentlyUpdatedContactList = listOf(contact(name = "이영희"), contact(name = "김철수"))
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
    }

    private companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun viewModel(pageContactUseCase: PageContactUseCase): ContactHomeViewModel = ContactHomeViewModel(pageContactUseCase = pageContactUseCase)

        private fun pageContactUseCase(contactListFlow: Flow<Result<List<Contact>>>): PageContactUseCase {
            val pageContactUseCase = mockk<PageContactUseCase>()
            every { pageContactUseCase(parameter = ListSort.NAME) } returns
                contactListFlow.map { result -> result.map { contactList -> PagingData.from(contactList) } }

            return pageContactUseCase
        }

        // FixtureMonkey가 Instant를 생성하지 못하므로 연락처는 직접 만든다.
        private fun contact(name: String): Contact {
            val detail =
                fixtureMonkey
                    .giveMeKotlinBuilder<ContactDetail>()
                    .setExp(ContactDetail::name, name)
                    .sample()

            return Contact(
                id = Uuid.random(),
                detail = detail,
                isDeleted = false,
                updatedAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
                createdAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
            )
        }
    }
}
