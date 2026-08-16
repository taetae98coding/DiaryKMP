@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.contact.ui.detail

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.domain.contact.exception.ContactPhoneNumberBlankException
import io.github.taetae98coding.diary.domain.contact.usecase.DeleteContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.FindContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.UpdateContactUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Instant
import kotlin.uuid.Uuid

class ContactDetailViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-CONTACT-DETAIL-FEATURE-001 조회에 성공하면 저장된 내용을 내용 표시 상태로 노출한다") {
            runTest(mainDispatcher) {
                val contact = contact()
                val viewModel = viewModel(id = contact.id, contactFlow = flowOf(Result.success(contact)))

                viewModel.uiState.test {
                    awaitItem() shouldBe ContactDetailUiState.Loading
                    awaitItem() shouldBe ContactDetailUiState.Content(id = contact.id, detail = contact.detail)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-CONTACT-DETAIL-FEATURE-005 연락처를 조회할 수 없으면 조회 중 상태를 유지한다") {
            runTest(mainDispatcher) {
                val id = fixtureMonkey.giveMeOne<Uuid>()

                listOf(
                    Result.failure(IllegalStateException()),
                    Result.success(null),
                ).forEach { result ->
                    val viewModel = viewModel(id = id, contactFlow = flowOf(result))

                    viewModel.uiState.test {
                        awaitItem() shouldBe ContactDetailUiState.Loading
                        expectNoEvents()
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        test("TC-CONTACT-DETAIL-FEATURE-007 수정에 성공하면 성공 결과를 알린다") {
            runTest(mainDispatcher) {
                val contact = contact()
                val updateContactUseCase = mockk<UpdateContactUseCase>()
                coEvery { updateContactUseCase(parameter = any()) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = contact.id,
                        contactFlow = flowOf(Result.success(contact)),
                        updateContactUseCase = updateContactUseCase,
                    )

                viewModel.effect.test {
                    viewModel.update(detail = contact.detail)
                    advanceUntilIdle()

                    awaitItem() shouldBe ContactDetailEffect.UpdateSucceeded
                    expectNoEvents()
                }

                coVerify(exactly = 1) {
                    updateContactUseCase(parameter = UpdateContactUseCase.Parameter(id = contact.id, detail = contact.detail))
                }
            }
        }

        test("TC-CONTACT-DETAIL-FEATURE-012 번호가 비어 있으면 전화번호 입력이 필요함을 알린다") {
            runTest(mainDispatcher) {
                val contact = contact()
                val updateContactUseCase = mockk<UpdateContactUseCase>()
                coEvery { updateContactUseCase(parameter = any()) } returns Result.failure(ContactPhoneNumberBlankException())
                val viewModel =
                    viewModel(
                        id = contact.id,
                        contactFlow = flowOf(Result.success(contact)),
                        updateContactUseCase = updateContactUseCase,
                    )

                viewModel.effect.test {
                    viewModel.update(detail = contact.detail)
                    advanceUntilIdle()

                    awaitItem() shouldBe ContactDetailEffect.PhoneNumberBlank
                    expectNoEvents()
                }
            }
        }

        test("TC-CONTACT-DETAIL-FEATURE-009 수정에 실패하면 성공을 알리지 않는다") {
            runTest(mainDispatcher) {
                val contact = contact()
                val updateContactUseCase = mockk<UpdateContactUseCase>()
                coEvery { updateContactUseCase(parameter = any()) } returns Result.failure(IllegalStateException())
                val viewModel =
                    viewModel(
                        id = contact.id,
                        contactFlow = flowOf(Result.success(contact)),
                        updateContactUseCase = updateContactUseCase,
                    )

                viewModel.effect.test {
                    viewModel.update(detail = contact.detail)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-CONTACT-DETAIL-FEATURE-008 수정을 처리하는 동안 진행 상태를 노출한다") {
            runTest(mainDispatcher) {
                val contact = contact()
                val deferred = CompletableDeferred<Result<Int>>()
                val updateContactUseCase = mockk<UpdateContactUseCase>()
                coEvery { updateContactUseCase(parameter = any()) } coAnswers { deferred.await() }
                val viewModel =
                    viewModel(
                        id = contact.id,
                        contactFlow = flowOf(Result.success(contact)),
                        updateContactUseCase = updateContactUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe ContactDetailUiState.Loading
                    awaitItem() shouldBe ContactDetailUiState.Content(id = contact.id, detail = contact.detail)

                    viewModel.update(detail = contact.detail)
                    advanceUntilIdle()

                    awaitItem() shouldBe
                        ContactDetailUiState.Content(id = contact.id, detail = contact.detail, isUpdateInProgress = true)

                    deferred.complete(Result.success(1))
                    advanceUntilIdle()

                    awaitItem() shouldBe ContactDetailUiState.Content(id = contact.id, detail = contact.detail)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-CONTACT-DETAIL-DOMAIN-011 수정을 처리하는 동안 같은 수정 요청은 처리하지 않는다") {
            runTest(mainDispatcher) {
                val contact = contact()
                val deferred = CompletableDeferred<Result<Int>>()
                val updateContactUseCase = mockk<UpdateContactUseCase>()
                coEvery { updateContactUseCase(parameter = any()) } coAnswers { deferred.await() }
                val viewModel =
                    viewModel(
                        id = contact.id,
                        contactFlow = flowOf(Result.success(contact)),
                        updateContactUseCase = updateContactUseCase,
                    )

                viewModel.update(detail = contact.detail)
                advanceUntilIdle()
                viewModel.update(detail = contact.detail)
                advanceUntilIdle()

                coVerify(exactly = 1) { updateContactUseCase(parameter = any()) }
                deferred.complete(Result.success(1))
            }
        }

        test("TC-CONTACT-DETAIL-DOMAIN-011 수정을 처리하는 동안에도 삭제는 처리한다") {
            runTest(mainDispatcher) {
                val contact = contact()
                val updateDeferred = CompletableDeferred<Result<Int>>()
                val updateContactUseCase = mockk<UpdateContactUseCase>()
                coEvery { updateContactUseCase(parameter = any()) } coAnswers { updateDeferred.await() }
                val deleteContactUseCase = mockk<DeleteContactUseCase>()
                coEvery { deleteContactUseCase(parameter = contact.id) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = contact.id,
                        contactFlow = flowOf(Result.success(contact)),
                        updateContactUseCase = updateContactUseCase,
                        deleteContactUseCase = deleteContactUseCase,
                    )

                viewModel.update(detail = contact.detail)
                advanceUntilIdle()
                viewModel.delete()
                advanceUntilIdle()

                coVerify(exactly = 1) { deleteContactUseCase(parameter = contact.id) }
                updateDeferred.complete(Result.success(1))
            }
        }

        test("TC-CONTACT-DETAIL-FEATURE-013 삭제에 성공하면 성공 결과를 알린다") {
            runTest(mainDispatcher) {
                val contact = contact()
                val deleteContactUseCase = mockk<DeleteContactUseCase>()
                coEvery { deleteContactUseCase(parameter = contact.id) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = contact.id,
                        contactFlow = flowOf(Result.success(contact)),
                        deleteContactUseCase = deleteContactUseCase,
                    )

                viewModel.effect.test {
                    viewModel.delete()
                    advanceUntilIdle()

                    awaitItem() shouldBe ContactDetailEffect.DeleteSucceeded
                    expectNoEvents()
                }
            }
        }

        test("TC-CONTACT-DETAIL-FEATURE-015 삭제에 실패하면 성공을 알리지 않는다") {
            runTest(mainDispatcher) {
                val contact = contact()
                val deleteContactUseCase = mockk<DeleteContactUseCase>()
                coEvery { deleteContactUseCase(parameter = contact.id) } returns Result.failure(IllegalStateException())
                val viewModel =
                    viewModel(
                        id = contact.id,
                        contactFlow = flowOf(Result.success(contact)),
                        deleteContactUseCase = deleteContactUseCase,
                    )

                viewModel.effect.test {
                    viewModel.delete()
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-CONTACT-DETAIL-FEATURE-014 삭제를 처리하는 동안 진행 상태를 노출한다") {
            runTest(mainDispatcher) {
                val contact = contact()
                val deferred = CompletableDeferred<Result<Int>>()
                val deleteContactUseCase = mockk<DeleteContactUseCase>()
                coEvery { deleteContactUseCase(parameter = contact.id) } coAnswers { deferred.await() }
                val viewModel =
                    viewModel(
                        id = contact.id,
                        contactFlow = flowOf(Result.success(contact)),
                        deleteContactUseCase = deleteContactUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe ContactDetailUiState.Loading
                    awaitItem() shouldBe ContactDetailUiState.Content(id = contact.id, detail = contact.detail)

                    viewModel.delete()
                    advanceUntilIdle()

                    awaitItem() shouldBe
                        ContactDetailUiState.Content(id = contact.id, detail = contact.detail, isDeleteInProgress = true)

                    deferred.complete(Result.success(1))
                    advanceUntilIdle()

                    awaitItem() shouldBe ContactDetailUiState.Content(id = contact.id, detail = contact.detail)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-CONTACT-DETAIL-FEATURE-018 대상 연락처가 삭제 상태가 되어도 내용 표시 상태를 유지한다") {
            runTest(mainDispatcher) {
                val contact = contact().copy(isDeleted = true)
                val viewModel = viewModel(id = contact.id, contactFlow = flowOf(Result.success(contact)))

                viewModel.uiState.test {
                    awaitItem() shouldBe ContactDetailUiState.Loading
                    awaitItem() shouldBe ContactDetailUiState.Content(id = contact.id, detail = contact.detail)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    private companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun viewModel(
            id: Uuid,
            contactFlow: Flow<Result<Contact?>>,
            updateContactUseCase: UpdateContactUseCase = mockk(),
            deleteContactUseCase: DeleteContactUseCase = mockk(),
        ): ContactDetailViewModel {
            val findContactUseCase = mockk<FindContactUseCase>()
            every { findContactUseCase(parameter = id) } returns contactFlow

            return ContactDetailViewModel(
                id = id,
                updateContactUseCase = updateContactUseCase,
                deleteContactUseCase = deleteContactUseCase,
                findContactUseCase = findContactUseCase,
            )
        }

        // FixtureMonkey가 Instant를 생성하지 못하므로 연락처는 직접 만든다.
        private fun contact(): Contact =
            Contact(
                id = fixtureMonkey.giveMeOne<Uuid>(),
                detail = fixtureMonkey.giveMeKotlinBuilder<ContactDetail>().sample(),
                isDeleted = false,
                updatedAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
                createdAt = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()),
            )
    }
}
