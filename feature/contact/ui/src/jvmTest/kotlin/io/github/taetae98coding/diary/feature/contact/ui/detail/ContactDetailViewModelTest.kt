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
import io.github.taetae98coding.diary.domain.contact.usecase.FavoriteContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.FindContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.UnfavoriteContactUseCase
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
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

                viewModel.uiState.test {
                    awaitItem() shouldBe ContactDetailUiState.Loading
                    awaitItem() shouldBe ContactDetailUiState.Content(id = contact.id, detail = contact.detail)
                    cancelAndIgnoreRemainingEvents()
                }

                viewModel.update(detail = contact.detail)
                advanceUntilIdle()

                coVerify(exactly = 2) { updateContactUseCase(parameter = UpdateContactUseCase.Parameter(id = contact.id, detail = contact.detail)) }
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

                viewModel.uiState.test {
                    awaitItem() shouldBe ContactDetailUiState.Loading
                    awaitItem() shouldBe ContactDetailUiState.Content(id = contact.id, detail = contact.detail)
                    cancelAndIgnoreRemainingEvents()
                }

                viewModel.delete()
                advanceUntilIdle()

                coVerify(exactly = 2) { deleteContactUseCase(parameter = contact.id) }
            }
        }

        test("TC-CONTACT-DETAIL-DOMAIN-011 삭제를 처리하는 동안 같은 삭제 요청은 처리하지 않는다") {
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

                viewModel.delete()
                advanceUntilIdle()
                viewModel.delete()
                advanceUntilIdle()

                coVerify(exactly = 1) { deleteContactUseCase(parameter = contact.id) }
                deferred.complete(Result.success(1))
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

        test("TC-CONTACT-DETAIL-FEATURE-025 저장된 즐겨찾기 여부를 그대로 노출한다") {
            runTest(mainDispatcher) {
                listOf(true, false).forEach { isFavorite ->
                    val contact = contact().copy(isFavorite = isFavorite)
                    val viewModel = viewModel(id = contact.id, contactFlow = flowOf(Result.success(contact)))

                    viewModel.uiState.test {
                        awaitItem() shouldBe ContactDetailUiState.Loading
                        awaitItem() shouldBe ContactDetailUiState.Content(id = contact.id, detail = contact.detail, isFavorite = isFavorite)
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        test("TC-CONTACT-DETAIL-FEATURE-026 즐겨찾기가 아닌 연락처의 즐겨찾기를 바꾸면 즐겨찾기로 저장되고 즐겨찾기 상태가 된다") {
            runTest(mainDispatcher) {
                val contact = contact().copy(isFavorite = false)
                val contactFlow = MutableStateFlow<Result<Contact?>>(Result.success(contact))
                val favoriteContactUseCase = mockk<FavoriteContactUseCase>()
                coEvery { favoriteContactUseCase(parameter = contact.id) } coAnswers {
                    contactFlow.value = Result.success(contact.copy(isFavorite = true))
                    Result.success(1)
                }
                val viewModel = viewModel(id = contact.id, contactFlow = contactFlow, favoriteContactUseCase = favoriteContactUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe ContactDetailUiState.Loading
                    awaitItem() shouldBe ContactDetailUiState.Content(id = contact.id, detail = contact.detail, isFavorite = false)

                    viewModel.toggleFavorite()
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe ContactDetailUiState.Content(id = contact.id, detail = contact.detail, isFavorite = true)
                    cancelAndIgnoreRemainingEvents()
                }

                coVerify(exactly = 1) { favoriteContactUseCase(parameter = contact.id) }
            }
        }

        test("TC-CONTACT-DETAIL-FEATURE-026 즐겨찾기인 연락처의 즐겨찾기를 바꾸면 즐겨찾기가 해제된다") {
            runTest(mainDispatcher) {
                val contact = contact().copy(isFavorite = true)
                val contactFlow = MutableStateFlow<Result<Contact?>>(Result.success(contact))
                val unfavoriteContactUseCase = mockk<UnfavoriteContactUseCase>()
                coEvery { unfavoriteContactUseCase(parameter = contact.id) } coAnswers {
                    contactFlow.value = Result.success(contact.copy(isFavorite = false))
                    Result.success(1)
                }
                val viewModel = viewModel(id = contact.id, contactFlow = contactFlow, unfavoriteContactUseCase = unfavoriteContactUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe ContactDetailUiState.Loading
                    awaitItem() shouldBe ContactDetailUiState.Content(id = contact.id, detail = contact.detail, isFavorite = true)

                    viewModel.toggleFavorite()
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe ContactDetailUiState.Content(id = contact.id, detail = contact.detail, isFavorite = false)
                    cancelAndIgnoreRemainingEvents()
                }

                coVerify(exactly = 1) { unfavoriteContactUseCase(parameter = contact.id) }
            }
        }

        test("TC-CONTACT-DETAIL-FEATURE-028 즐겨찾기 변경을 처리하는 동안 진행 상태를 노출한다") {
            runTest(mainDispatcher) {
                val contact = contact().copy(isFavorite = false)
                val deferred = CompletableDeferred<Result<Int>>()
                val favoriteContactUseCase = mockk<FavoriteContactUseCase>()
                coEvery { favoriteContactUseCase(parameter = contact.id) } coAnswers { deferred.await() }
                val viewModel = viewModel(id = contact.id, contactFlow = flowOf(Result.success(contact)), favoriteContactUseCase = favoriteContactUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe ContactDetailUiState.Loading
                    awaitItem() shouldBe ContactDetailUiState.Content(id = contact.id, detail = contact.detail)

                    viewModel.toggleFavorite()
                    advanceUntilIdle()

                    awaitItem() shouldBe
                        ContactDetailUiState.Content(id = contact.id, detail = contact.detail, isFavoriteInProgress = true)

                    deferred.complete(Result.success(1))
                    advanceUntilIdle()

                    awaitItem() shouldBe ContactDetailUiState.Content(id = contact.id, detail = contact.detail)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-CONTACT-DETAIL-FEATURE-029 즐겨찾기 변경에 성공해도 알리는 결과를 내보내지 않는다") {
            runTest(mainDispatcher) {
                val contact = contact().copy(isFavorite = false)
                val favoriteContactUseCase = mockk<FavoriteContactUseCase>()
                coEvery { favoriteContactUseCase(parameter = contact.id) } returns Result.success(1)
                val viewModel = viewModel(id = contact.id, contactFlow = flowOf(Result.success(contact)), favoriteContactUseCase = favoriteContactUseCase)

                backgroundScope.launch { viewModel.uiState.collect { } }
                advanceUntilIdle()

                viewModel.effect.test {
                    viewModel.toggleFavorite()
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-CONTACT-DETAIL-FEATURE-030 즐겨찾기 변경에 실패하면 진행 상태만 해제하고 저장된 값을 유지한다") {
            runTest(mainDispatcher) {
                val contact = contact().copy(isFavorite = false)
                val favoriteContactUseCase = mockk<FavoriteContactUseCase>()
                coEvery { favoriteContactUseCase(parameter = contact.id) } returns Result.failure(IllegalStateException())
                val viewModel = viewModel(id = contact.id, contactFlow = flowOf(Result.success(contact)), favoriteContactUseCase = favoriteContactUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe ContactDetailUiState.Loading
                    awaitItem() shouldBe ContactDetailUiState.Content(id = contact.id, detail = contact.detail, isFavorite = false)

                    viewModel.toggleFavorite()
                    advanceUntilIdle()

                    viewModel.uiState.value shouldBe ContactDetailUiState.Content(id = contact.id, detail = contact.detail, isFavorite = false)
                    cancelAndIgnoreRemainingEvents()
                }

                viewModel.effect.test {
                    expectNoEvents()
                }
            }
        }

        test("TC-CONTACT-DETAIL-FEATURE-031 저장된 즐겨찾기가 다른 경로로 바뀌면 상태도 갱신된다") {
            runTest(mainDispatcher) {
                val contact = contact().copy(isFavorite = false)
                val contactFlow = MutableStateFlow<Result<Contact?>>(Result.success(contact))
                val viewModel = viewModel(id = contact.id, contactFlow = contactFlow)

                viewModel.uiState.test {
                    awaitItem() shouldBe ContactDetailUiState.Loading
                    awaitItem() shouldBe ContactDetailUiState.Content(id = contact.id, detail = contact.detail, isFavorite = false)

                    contactFlow.value = Result.success(contact.copy(isFavorite = true))
                    advanceUntilIdle()

                    awaitItem() shouldBe ContactDetailUiState.Content(id = contact.id, detail = contact.detail, isFavorite = true)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-CONTACT-DETAIL-DOMAIN-011 즐겨찾기 변경을 처리하는 동안 같은 즐겨찾기 변경 요청은 처리하지 않는다") {
            runTest(mainDispatcher) {
                val contact = contact().copy(isFavorite = false)
                val deferred = CompletableDeferred<Result<Int>>()
                val favoriteContactUseCase = mockk<FavoriteContactUseCase>()
                coEvery { favoriteContactUseCase(parameter = contact.id) } coAnswers { deferred.await() }
                val viewModel = viewModel(id = contact.id, contactFlow = flowOf(Result.success(contact)), favoriteContactUseCase = favoriteContactUseCase)

                backgroundScope.launch { viewModel.uiState.collect { } }
                advanceUntilIdle()

                viewModel.toggleFavorite()
                advanceUntilIdle()
                viewModel.toggleFavorite()
                advanceUntilIdle()

                coVerify(exactly = 1) { favoriteContactUseCase(parameter = contact.id) }
                deferred.complete(Result.success(1))
            }
        }

        test("TC-CONTACT-DETAIL-DOMAIN-011 즐겨찾기 변경을 처리하는 동안에도 수정과 삭제는 처리한다") {
            runTest(mainDispatcher) {
                val contact = contact().copy(isFavorite = false)
                val favoriteDeferred = CompletableDeferred<Result<Int>>()
                val favoriteContactUseCase = mockk<FavoriteContactUseCase>()
                coEvery { favoriteContactUseCase(parameter = contact.id) } coAnswers { favoriteDeferred.await() }
                val updateContactUseCase = mockk<UpdateContactUseCase>()
                coEvery { updateContactUseCase(parameter = any()) } returns Result.success(1)
                val deleteContactUseCase = mockk<DeleteContactUseCase>()
                coEvery { deleteContactUseCase(parameter = contact.id) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = contact.id,
                        contactFlow = flowOf(Result.success(contact)),
                        updateContactUseCase = updateContactUseCase,
                        favoriteContactUseCase = favoriteContactUseCase,
                        deleteContactUseCase = deleteContactUseCase,
                    )

                backgroundScope.launch { viewModel.uiState.collect { } }
                advanceUntilIdle()

                viewModel.toggleFavorite()
                advanceUntilIdle()
                viewModel.update(detail = contact.detail)
                advanceUntilIdle()
                viewModel.delete()
                advanceUntilIdle()

                coVerify(exactly = 1) { updateContactUseCase(parameter = any()) }
                coVerify(exactly = 1) { deleteContactUseCase(parameter = contact.id) }
                favoriteDeferred.complete(Result.success(1))
            }
        }

        test("TC-CONTACT-DETAIL-DOMAIN-011 수정을 처리하는 동안에도 즐겨찾기 변경은 처리한다") {
            runTest(mainDispatcher) {
                val contact = contact().copy(isFavorite = false)
                val updateDeferred = CompletableDeferred<Result<Int>>()
                val updateContactUseCase = mockk<UpdateContactUseCase>()
                coEvery { updateContactUseCase(parameter = any()) } coAnswers { updateDeferred.await() }
                val favoriteContactUseCase = mockk<FavoriteContactUseCase>()
                coEvery { favoriteContactUseCase(parameter = contact.id) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = contact.id,
                        contactFlow = flowOf(Result.success(contact)),
                        updateContactUseCase = updateContactUseCase,
                        favoriteContactUseCase = favoriteContactUseCase,
                    )

                backgroundScope.launch { viewModel.uiState.collect { } }
                advanceUntilIdle()

                viewModel.update(detail = contact.detail)
                advanceUntilIdle()
                viewModel.toggleFavorite()
                advanceUntilIdle()

                coVerify(exactly = 1) { favoriteContactUseCase(parameter = contact.id) }
                updateDeferred.complete(Result.success(1))
            }
        }

        test("TC-CONTACT-DETAIL-DOMAIN-011 삭제를 처리하는 동안에도 수정과 즐겨찾기 변경은 처리한다") {
            runTest(mainDispatcher) {
                val contact = contact().copy(isFavorite = false)
                val deleteDeferred = CompletableDeferred<Result<Int>>()
                val deleteContactUseCase = mockk<DeleteContactUseCase>()
                coEvery { deleteContactUseCase(parameter = contact.id) } coAnswers { deleteDeferred.await() }
                val updateContactUseCase = mockk<UpdateContactUseCase>()
                coEvery { updateContactUseCase(parameter = any()) } returns Result.success(1)
                val favoriteContactUseCase = mockk<FavoriteContactUseCase>()
                coEvery { favoriteContactUseCase(parameter = contact.id) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = contact.id,
                        contactFlow = flowOf(Result.success(contact)),
                        updateContactUseCase = updateContactUseCase,
                        favoriteContactUseCase = favoriteContactUseCase,
                        deleteContactUseCase = deleteContactUseCase,
                    )

                backgroundScope.launch { viewModel.uiState.collect { } }
                advanceUntilIdle()

                viewModel.delete()
                advanceUntilIdle()
                viewModel.update(detail = contact.detail)
                advanceUntilIdle()
                viewModel.toggleFavorite()
                advanceUntilIdle()

                coVerify(exactly = 1) { updateContactUseCase(parameter = any()) }
                coVerify(exactly = 1) { favoriteContactUseCase(parameter = contact.id) }
                deleteDeferred.complete(Result.success(1))
            }
        }

        test("TC-CONTACT-DETAIL-FEATURE-018 대상 연락처가 삭제 상태로 바뀌어도 내용 표시 상태를 유지하고 수정과 삭제를 처리한다") {
            runTest(mainDispatcher) {
                val contact = contact()
                val contactFlow = MutableStateFlow(Result.success<Contact?>(contact))
                val updateContactUseCase = mockk<UpdateContactUseCase>()
                coEvery { updateContactUseCase(parameter = any()) } returns Result.success(1)
                val deleteContactUseCase = mockk<DeleteContactUseCase>()
                coEvery { deleteContactUseCase(parameter = contact.id) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = contact.id,
                        contactFlow = contactFlow,
                        updateContactUseCase = updateContactUseCase,
                        deleteContactUseCase = deleteContactUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe ContactDetailUiState.Loading
                    awaitItem() shouldBe ContactDetailUiState.Content(id = contact.id, detail = contact.detail)

                    contactFlow.value = Result.success(contact.copy(isDeleted = true))
                    advanceUntilIdle()

                    expectNoEvents()
                    viewModel.uiState.value shouldBe ContactDetailUiState.Content(id = contact.id, detail = contact.detail)

                    viewModel.update(detail = contact.detail)
                    advanceUntilIdle()
                    viewModel.delete()
                    advanceUntilIdle()
                    cancelAndIgnoreRemainingEvents()
                }

                coVerify(exactly = 1) { updateContactUseCase(parameter = UpdateContactUseCase.Parameter(id = contact.id, detail = contact.detail)) }
                coVerify(exactly = 1) { deleteContactUseCase(parameter = contact.id) }
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
            favoriteContactUseCase: FavoriteContactUseCase = mockk(),
            unfavoriteContactUseCase: UnfavoriteContactUseCase = mockk(),
            deleteContactUseCase: DeleteContactUseCase = mockk(),
        ): ContactDetailViewModel {
            val findContactUseCase = mockk<FindContactUseCase>()
            every { findContactUseCase(parameter = id) } returns contactFlow

            return ContactDetailViewModel(
                id = id,
                updateContactUseCase = updateContactUseCase,
                favoriteContactUseCase = favoriteContactUseCase,
                unfavoriteContactUseCase = unfavoriteContactUseCase,
                deleteContactUseCase = deleteContactUseCase,
                findContactUseCase = findContactUseCase,
            )
        }

        private fun contact(): Contact =
            Contact(
                id = fixtureMonkey.giveMeOne<Uuid>(),
                detail = fixtureMonkey.giveMeKotlinBuilder<ContactDetail>().sample(),
                isFavorite = false,
                isDeleted = false,
                updatedAt = fixtureMonkey.giveMeOne<Instant>(),
                createdAt = fixtureMonkey.giveMeOne<Instant>(),
            )
    }
}
