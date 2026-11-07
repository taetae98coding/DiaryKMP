@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.account.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.authentication.AppleAuthorization
import io.github.taetae98coding.diary.core.testing.authentication.appleAuthorization
import io.github.taetae98coding.diary.domain.account.usecase.CheckAppleReauthenticationRequiredUseCase
import io.github.taetae98coding.diary.domain.sync.usecase.DeleteAccountUseCase
import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.core.DiaryLoggerDelegate
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class ProfileDeleteViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-265aa834-9a55-4d5b-83db-a892be445edc TC-a82417c6-2e09-4a8a-a181-c3490c24f6d7 확인을 열고 닫아도 탈퇴를 요청하지 않는다") {
            runTest(mainDispatcher) {
                val deleteAccountUseCase = deleteAccountUseCase()
                val viewModel = viewModel(deleteAccountUseCase = deleteAccountUseCase)

                viewModel.showConfirm()
                viewModel.uiState.value shouldBe ProfileDeleteUiState(isConfirmVisible = true)

                viewModel.dismissConfirm()
                advanceUntilIdle()

                viewModel.uiState.value shouldBe ProfileDeleteUiState()
                coVerify(exactly = 0) { deleteAccountUseCase(parameter = any()) }
            }
        }

        test("TC-c94e0258-ad3d-4541-af01-4070488bca25 확인 다이얼로그의 열림은 저장된 상태에서 되살아난다") {
            runTest(mainDispatcher) {
                val savedStateHandle = SavedStateHandle()
                viewModel(savedStateHandle = savedStateHandle).showConfirm()

                viewModel(savedStateHandle = savedStateHandle).uiState.value.isConfirmVisible shouldBe true

                viewModel(savedStateHandle = savedStateHandle).dismissConfirm()

                viewModel(savedStateHandle = savedStateHandle).uiState.value.isConfirmVisible shouldBe false
            }
        }

        test("TC-394d73c1-14ac-40cc-8355-db7b3bc510ec 재인증이 필요하면 탈퇴를 요청하지 않고 Apple 재인증을 요청한다") {
            runTest(mainDispatcher) {
                val deleteAccountUseCase = deleteAccountUseCase()
                val viewModel = viewModel(checkUseCase = checkUseCase(isRequired = true), deleteAccountUseCase = deleteAccountUseCase)

                viewModel.effect.test {
                    viewModel.showConfirm()
                    viewModel.confirmDelete()
                    advanceUntilIdle()

                    awaitItem() shouldBe ProfileDeleteEffect.RequestAppleAuthorization
                }

                coVerify(exactly = 0) { deleteAccountUseCase(parameter = any()) }
            }
        }

        test("TC-364abafa-b866-4902-b128-c8a3799b563f Apple 재인증을 취소하면 오류 보고를 남기지 않는다") {
            runTest(mainDispatcher) {
                val reportList = recordCrashlyticsLog()
                val viewModel = viewModel(checkUseCase = checkUseCase(isRequired = true))

                viewModel.showConfirm()
                viewModel.confirmDelete()
                advanceUntilIdle()
                viewModel.endAppleAuthorization()
                advanceUntilIdle()

                reportList.shouldBeEmpty()
            }
        }

        test("TC-364abafa-b866-4902-b128-c8a3799b563f TC-30f87464-4507-40d0-aafe-b02ec5314b62 오류 없이 인가 코드 없이 끝나면 실패를 알리되 오류 보고는 남기지 않는다") {
            runTest(mainDispatcher) {
                val reportList = recordCrashlyticsLog()
                val deleteAccountUseCase = deleteAccountUseCase()
                val viewModel = viewModel(checkUseCase = checkUseCase(isRequired = true), deleteAccountUseCase = deleteAccountUseCase)

                viewModel.effect.test {
                    viewModel.showConfirm()
                    viewModel.confirmDelete()
                    advanceUntilIdle()
                    awaitItem() shouldBe ProfileDeleteEffect.RequestAppleAuthorization

                    viewModel.failAppleAuthorizationWithoutCode()
                    advanceUntilIdle()

                    awaitItem() shouldBe ProfileDeleteEffect.DeleteFailed
                }

                reportList.shouldBeEmpty()
                viewModel.uiState.value shouldBe ProfileDeleteUiState()
                coVerify(exactly = 0) { deleteAccountUseCase(parameter = any()) }
            }
        }

        test("TC-9c107475-8706-4131-a031-06dd7fd577b4 Apple 재인증 중 앱이 다시 시작되면 다이얼로그를 닫은 탈퇴 대기로 시작한다") {
            runTest(mainDispatcher) {
                val savedStateHandle = SavedStateHandle()
                val viewModel = viewModel(savedStateHandle = savedStateHandle, checkUseCase = checkUseCase(isRequired = true))

                viewModel.showConfirm()
                viewModel.confirmDelete()
                advanceUntilIdle()

                val deleteAccountUseCase = deleteAccountUseCase()
                val restarted = viewModel(savedStateHandle = savedStateHandle, deleteAccountUseCase = deleteAccountUseCase)

                restarted.effect.test {
                    advanceUntilIdle()

                    restarted.uiState.value shouldBe ProfileDeleteUiState()
                    expectNoEvents()

                    restarted.showConfirm()
                    restarted.uiState.value shouldBe ProfileDeleteUiState(isConfirmVisible = true)
                }
                coVerify(exactly = 0) { deleteAccountUseCase(parameter = any()) }
            }
        }

        test("TC-5b7e2c94-3d1a-4f68-9a0e-7c4d81b6e253 재인증 필요 여부를 판단하는 중 앱이 다시 시작되면 다이얼로그가 열린 채 복원되고 처리는 이어지지 않는다") {
            runTest(mainDispatcher) {
                val savedStateHandle = SavedStateHandle()
                val checkUseCase = mockk<CheckAppleReauthenticationRequiredUseCase>()
                coEvery { checkUseCase(parameter = Unit) } coAnswers { CompletableDeferred<Result<Boolean>>().await() }
                val deleteAccountUseCase = deleteAccountUseCase()
                val viewModel = viewModel(savedStateHandle = savedStateHandle, checkUseCase = checkUseCase, deleteAccountUseCase = deleteAccountUseCase)

                viewModel.showConfirm()
                viewModel.confirmDelete()
                advanceUntilIdle()

                val restarted = viewModel(savedStateHandle = savedStateHandle, checkUseCase = checkUseCase, deleteAccountUseCase = deleteAccountUseCase)
                restarted.effect.test {
                    advanceUntilIdle()

                    restarted.uiState.value shouldBe ProfileDeleteUiState(isConfirmVisible = true, isInProgress = false)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { checkUseCase(parameter = Unit) }
                coVerify(exactly = 0) { deleteAccountUseCase(parameter = any()) }

                restarted.dismissConfirm()
                viewModel(savedStateHandle = savedStateHandle).uiState.value shouldBe ProfileDeleteUiState()
            }
        }

        test("TC-6f5044df-99bd-4c87-ae5d-32cac969d642 탈퇴 처리 중 앱이 다시 시작되면 다이얼로그가 열린 채 복원되고 처리는 이어지지 않는다") {
            runTest(mainDispatcher) {
                val savedStateHandle = SavedStateHandle()
                val deleteAccountUseCase = mockk<DeleteAccountUseCase>()
                coEvery { deleteAccountUseCase(parameter = any()) } coAnswers { CompletableDeferred<Result<Unit>>().await() }
                val viewModel = viewModel(savedStateHandle = savedStateHandle, deleteAccountUseCase = deleteAccountUseCase)

                viewModel.showConfirm()
                viewModel.confirmDelete()
                advanceUntilIdle()

                val restarted = viewModel(savedStateHandle = savedStateHandle, deleteAccountUseCase = deleteAccountUseCase)
                advanceUntilIdle()

                restarted.uiState.value shouldBe ProfileDeleteUiState(isConfirmVisible = true, isInProgress = false)
                coVerify(exactly = 1) { deleteAccountUseCase(parameter = any()) }
            }
        }

        test("TC-b28f7750-5460-48cc-927e-e6e0ab50a3a9 Apple 재인증이 취소되면 진행 중을 풀고 탈퇴하지 않는다") {
            runTest(mainDispatcher) {
                val deleteAccountUseCase = deleteAccountUseCase()
                val viewModel = viewModel(checkUseCase = checkUseCase(isRequired = true), deleteAccountUseCase = deleteAccountUseCase)

                viewModel.showConfirm()
                viewModel.confirmDelete()
                advanceUntilIdle()
                viewModel.endAppleAuthorization()

                viewModel.uiState.value shouldBe ProfileDeleteUiState()
                coVerify(exactly = 0) { deleteAccountUseCase(parameter = any()) }
            }
        }

        test("TC-ff12655f-9d05-45d5-941d-052f02a957c2 Apple 재인증을 요청하면 다이얼로그를 연 채 진행 중을 두지 않는다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(checkUseCase = checkUseCase(isRequired = true))

                viewModel.showConfirm()
                viewModel.confirmDelete()
                advanceUntilIdle()

                viewModel.uiState.value shouldBe ProfileDeleteUiState(isConfirmVisible = true, isInProgress = false)
            }
        }

        test("TC-2d841dee-ec2d-4eab-8783-63ab01d80be1 탈퇴 진행 중에는 다이얼로그를 닫지 않는다") {
            runTest(mainDispatcher) {
                val deleteAccountUseCase = mockk<DeleteAccountUseCase>()
                coEvery { deleteAccountUseCase(parameter = any()) } coAnswers { CompletableDeferred<Result<Unit>>().await() }
                val viewModel = viewModel(deleteAccountUseCase = deleteAccountUseCase)

                viewModel.showConfirm()
                viewModel.confirmDelete()
                advanceUntilIdle()
                viewModel.dismissConfirm()

                viewModel.uiState.value shouldBe ProfileDeleteUiState(isConfirmVisible = true, isInProgress = true)
            }
        }

        test("TC-0ad36304-87ed-4c7b-bf3c-34c2ff1b125e 재인증 필요 여부를 판단하지 못하면 탈퇴하지 않고 실패를 알린다") {
            runTest(mainDispatcher) {
                val checkUseCase = mockk<CheckAppleReauthenticationRequiredUseCase>()
                coEvery { checkUseCase(parameter = Unit) } returns Result.failure(IllegalStateException("read"))
                val deleteAccountUseCase = deleteAccountUseCase()
                val viewModel = viewModel(checkUseCase = checkUseCase, deleteAccountUseCase = deleteAccountUseCase)

                viewModel.effect.test {
                    viewModel.confirmDelete()
                    advanceUntilIdle()

                    awaitItem() shouldBe ProfileDeleteEffect.DeleteFailed
                }

                viewModel.uiState.value shouldBe ProfileDeleteUiState()
                coVerify(exactly = 0) { deleteAccountUseCase(parameter = any()) }
            }
        }

        test("TC-ed0014d8-5724-43b8-9211-311afb02c36a 재인증이 필요 없으면 Apple 인가 코드 없이 탈퇴를 요청한다") {
            runTest(mainDispatcher) {
                val deleteAccountUseCase = deleteAccountUseCase()
                val viewModel = viewModel(checkUseCase = checkUseCase(isRequired = false), deleteAccountUseCase = deleteAccountUseCase)

                viewModel.effect.test {
                    viewModel.showConfirm()
                    viewModel.confirmDelete()
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 1) { deleteAccountUseCase(parameter = null) }
            }
        }

        test("TC-394d73c1-14ac-40cc-8355-db7b3bc510ec 받은 Apple 인가 코드로 탈퇴를 요청한다") {
            runTest(mainDispatcher) {
                val authorization = Arb.appleAuthorization().next()
                val deleteAccountUseCase = deleteAccountUseCase()
                val viewModel = viewModel(deleteAccountUseCase = deleteAccountUseCase)

                viewModel.delete(appleAuthorization = authorization)
                advanceUntilIdle()

                coVerify(exactly = 1) { deleteAccountUseCase(parameter = authorization) }
            }
        }

        test("TC-01c0991e-d5bf-4e4a-9d1b-5713a21c8fe6 탈퇴 요청 중에는 진행 중을 알린다") {
            runTest(mainDispatcher) {
                val response = CompletableDeferred<Result<Unit>>()
                val deleteAccountUseCase = mockk<DeleteAccountUseCase>()
                coEvery { deleteAccountUseCase(parameter = any()) } coAnswers { response.await() }
                val viewModel = viewModel(deleteAccountUseCase = deleteAccountUseCase)

                viewModel.confirmDelete()
                advanceUntilIdle()

                viewModel.uiState.value.isInProgress shouldBe true

                response.complete(Result.success(Unit))
                advanceUntilIdle()

                viewModel.uiState.value.isInProgress shouldBe false
            }
        }

        test("TC-f002854c-6189-4577-b4fb-152dee7455c3 진행 중에 다시 요청해도 탈퇴는 한 번만 요청한다") {
            runTest(mainDispatcher) {
                val response = CompletableDeferred<Result<Unit>>()
                val deleteAccountUseCase = mockk<DeleteAccountUseCase>()
                coEvery { deleteAccountUseCase(parameter = any()) } coAnswers { response.await() }
                val viewModel = viewModel(deleteAccountUseCase = deleteAccountUseCase)

                viewModel.confirmDelete()
                advanceUntilIdle()
                viewModel.confirmDelete()
                viewModel.showConfirm()
                advanceUntilIdle()

                coVerify(exactly = 1) { deleteAccountUseCase(parameter = any()) }
                viewModel.uiState.value.isConfirmVisible shouldBe false
                response.complete(Result.success(Unit))
            }
        }

        test("TC-ac89843d-73fb-4ece-9f0b-6f8c58a5228c 화면을 떠나 ViewModel이 정리되면 탈퇴를 중단하고 실패를 알리지 않는다") {
            runTest(mainDispatcher) {
                val cancelled = CompletableDeferred<Unit>()
                val deleteAccountUseCase = mockk<DeleteAccountUseCase>()
                coEvery { deleteAccountUseCase(parameter = any()) } coAnswers {
                    try {
                        awaitCancellation()
                    } finally {
                        cancelled.complete(Unit)
                    }
                }
                val store = ViewModelStore()
                val viewModel =
                    ViewModelProvider.create(
                        store = store,
                        factory = viewModelFactory { initializer { viewModel(deleteAccountUseCase = deleteAccountUseCase) } },
                    )[ProfileDeleteViewModel::class]

                viewModel.effect.test {
                    viewModel.confirmDelete()
                    advanceUntilIdle()

                    store.clear()
                    advanceUntilIdle()

                    cancelled.isCompleted shouldBe true
                    expectNoEvents()
                }
            }
        }

        test("TC-ed9f47a1-176a-4d60-a181-06e62faa6583 탈퇴에 실패하면 실패를 알리고 다시 요청할 수 있다") {
            runTest(mainDispatcher) {
                val deleteAccountUseCase = mockk<DeleteAccountUseCase>()
                coEvery { deleteAccountUseCase(parameter = any()) } returns Result.failure(IllegalStateException("delete"))
                val viewModel = viewModel(deleteAccountUseCase = deleteAccountUseCase)

                viewModel.effect.test {
                    viewModel.showConfirm()
                    viewModel.confirmDelete()
                    advanceUntilIdle()

                    awaitItem() shouldBe ProfileDeleteEffect.DeleteFailed
                    viewModel.uiState.value shouldBe ProfileDeleteUiState()

                    viewModel.confirmDelete()
                    advanceUntilIdle()

                    awaitItem() shouldBe ProfileDeleteEffect.DeleteFailed
                }

                coVerify(exactly = 2) { deleteAccountUseCase(parameter = any()) }
            }
        }

        test("TC-13a4dded-2539-40aa-8637-ea6f4de0517b 탈퇴에 성공하면 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel()

                viewModel.effect.test {
                    viewModel.confirmDelete()
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-30f87464-4507-40d0-aafe-b02ec5314b62 TC-364abafa-b866-4902-b128-c8a3799b563f Apple 로그인 흐름이 오류로 끝나면 실패를 알리고 오류 보고를 남긴다") {
            runTest(mainDispatcher) {
                val failure = IllegalStateException("no code")
                val reportList = recordCrashlyticsLog()
                val deleteAccountUseCase = deleteAccountUseCase()
                val viewModel = viewModel(deleteAccountUseCase = deleteAccountUseCase)

                viewModel.effect.test {
                    viewModel.showConfirm()
                    viewModel.failAppleAuthorization(throwable = failure)
                    advanceUntilIdle()

                    awaitItem() shouldBe ProfileDeleteEffect.DeleteFailed
                }

                reportList.single().throwable shouldBe failure
                viewModel.uiState.value shouldBe ProfileDeleteUiState()
                coVerify(exactly = 0) { deleteAccountUseCase(parameter = any()) }
            }
        }

        test("TC-364abafa-b866-4902-b128-c8a3799b563f 확인을 열고 닫기만 하면 오류 보고가 남지 않는다") {
            runTest(mainDispatcher) {
                val reportList = recordCrashlyticsLog()
                val viewModel = viewModel()

                viewModel.showConfirm()
                viewModel.dismissConfirm()
                advanceUntilIdle()

                reportList.shouldBeEmpty()
            }
        }
    }
}

private fun deleteAccountUseCase(): DeleteAccountUseCase =
    mockk<DeleteAccountUseCase>().also { useCase ->
        coEvery { useCase(parameter = any()) } returns Result.success(Unit)
    }

private fun checkUseCase(isRequired: Boolean): CheckAppleReauthenticationRequiredUseCase =
    mockk<CheckAppleReauthenticationRequiredUseCase>().also { useCase ->
        coEvery { useCase(parameter = Unit) } returns Result.success(isRequired)
    }

private fun viewModel(
    savedStateHandle: SavedStateHandle = SavedStateHandle(),
    checkUseCase: CheckAppleReauthenticationRequiredUseCase = checkUseCase(isRequired = false),
    deleteAccountUseCase: DeleteAccountUseCase = deleteAccountUseCase(),
): ProfileDeleteViewModel =
    ProfileDeleteViewModel(
        savedStateHandle = savedStateHandle,
        checkAppleReauthenticationRequiredUseCase = checkUseCase,
        deleteAccountUseCase = deleteAccountUseCase,
    )

private fun recordCrashlyticsLog(): List<CrashlyticsLog> {
    val reportList = mutableListOf<CrashlyticsLog>()
    val delegate = mockk<DiaryLoggerDelegate>()

    every { delegate.log(log = any()) } answers {
        val log = firstArg<DiaryLog>()
        if (log is CrashlyticsLog) {
            reportList += log
        }
    }

    DiaryLogger.add(delegate = delegate)

    return reportList
}
