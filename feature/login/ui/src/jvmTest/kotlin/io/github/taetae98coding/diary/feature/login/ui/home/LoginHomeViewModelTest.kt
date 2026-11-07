@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.login.ui.home

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.authentication.AppleCredential
import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential
import io.github.taetae98coding.diary.core.testing.authentication.appleCredential
import io.github.taetae98coding.diary.core.testing.authentication.googleCredentialAuthorizationCode
import io.github.taetae98coding.diary.core.testing.authentication.googleCredentialIdToken
import io.github.taetae98coding.diary.domain.account.usecase.SignInWithAppleUseCase
import io.github.taetae98coding.diary.domain.account.usecase.SignInWithGoogleUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class LoginHomeViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-d33c5bf1-1f6d-4023-9333-20e81623d39c 로그인 진행 상태를 전환한다") {
            runTest(mainDispatcher) {
                val credential = Arb.googleCredentialIdToken().next()
                val completion = CompletableDeferred<Unit>()
                val googleUseCase = mockk<SignInWithGoogleUseCase>()
                val appleUseCase = mockk<SignInWithAppleUseCase>()
                coEvery { googleUseCase(any()) } coAnswers { Result.success(completion.await()) }
                val viewModel = loginHomeViewModel(googleUseCase, appleUseCase)

                viewModel.signInWithGoogle(credential)
                runCurrent()

                viewModel.uiState.value.isInProgress
                    .shouldBeTrue()

                completion.complete(Unit)
                advanceUntilIdle()

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-d33c5bf1-1f6d-4023-9333-20e81623d39c Apple 로그인 진행 상태를 전환한다") {
            runTest(mainDispatcher) {
                val credential = Arb.appleCredential().next()
                val completion = CompletableDeferred<Unit>()
                val googleUseCase = mockk<SignInWithGoogleUseCase>()
                val appleUseCase = mockk<SignInWithAppleUseCase>()
                coEvery { appleUseCase(any()) } coAnswers { Result.success(completion.await()) }
                val viewModel = loginHomeViewModel(googleUseCase, appleUseCase)

                viewModel.signInWithApple(credential)
                runCurrent()

                viewModel.uiState.value.isInProgress
                    .shouldBeTrue()

                completion.complete(Unit)
                advanceUntilIdle()

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-bf35c0ee-ee31-4ebd-8b72-937d90ccccda 진행 중 같은 수단의 추가 요청 무시") {
            runTest(mainDispatcher) {
                val firstCredential = Arb.googleCredentialIdToken().next()
                val secondCredential = Arb.googleCredentialAuthorizationCode().next()
                val completion = CompletableDeferred<Unit>()
                val googleUseCase = mockk<SignInWithGoogleUseCase>()
                val appleUseCase = mockk<SignInWithAppleUseCase>()
                coEvery { googleUseCase(any()) } coAnswers { Result.success(completion.await()) }
                val viewModel = loginHomeViewModel(googleUseCase, appleUseCase)

                viewModel.signInWithGoogle(firstCredential)
                runCurrent()

                viewModel.uiState.value.isInProgress
                    .shouldBeTrue()

                viewModel.signInWithGoogle(secondCredential)
                runCurrent()

                coVerify(exactly = 1) { googleUseCase(firstCredential) }
                coVerify(exactly = 0) { googleUseCase(secondCredential) }

                completion.complete(Unit)
                advanceUntilIdle()
            }
        }

        test("TC-bf35c0ee-ee31-4ebd-8b72-937d90ccccda 진행 중 다른 수단의 추가 요청 무시") {
            runTest(mainDispatcher) {
                val googleCredential = Arb.googleCredentialIdToken().next()
                val appleCredential = Arb.appleCredential().next()
                val completion = CompletableDeferred<Unit>()
                val googleUseCase = mockk<SignInWithGoogleUseCase>()
                val appleUseCase = mockk<SignInWithAppleUseCase>()
                coEvery { googleUseCase(any()) } coAnswers { Result.success(completion.await()) }
                val viewModel = loginHomeViewModel(googleUseCase, appleUseCase)

                viewModel.signInWithGoogle(googleCredential)
                runCurrent()

                viewModel.uiState.value.isInProgress
                    .shouldBeTrue()

                viewModel.signInWithApple(appleCredential)
                runCurrent()

                coVerify(exactly = 1) { googleUseCase(googleCredential) }
                coVerify(exactly = 0) { appleUseCase(any()) }

                completion.complete(Unit)
                advanceUntilIdle()
            }
        }

        test("성공 Effect를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val credential = Arb.googleCredentialIdToken().next()
                val googleUseCase = mockk<SignInWithGoogleUseCase>()
                val appleUseCase = mockk<SignInWithAppleUseCase>()
                coEvery { googleUseCase(any()) } returns Result.success(Unit)
                val viewModel = loginHomeViewModel(googleUseCase, appleUseCase)

                viewModel.effect.test {
                    viewModel.signInWithGoogle(credential)
                    advanceUntilIdle()

                    awaitItem() shouldBe LoginHomeEffect.SignInSucceeded
                    expectNoEvents()
                }
            }
        }

        test("Apple 로그인 성공 Effect를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val credential = Arb.appleCredential().next()
                val googleUseCase = mockk<SignInWithGoogleUseCase>()
                val appleUseCase = mockk<SignInWithAppleUseCase>()
                coEvery { appleUseCase(credential) } returns Result.success(Unit)
                val viewModel = loginHomeViewModel(googleUseCase, appleUseCase)

                viewModel.effect.test {
                    viewModel.signInWithApple(credential)
                    advanceUntilIdle()

                    awaitItem() shouldBe LoginHomeEffect.SignInSucceeded
                    expectNoEvents()
                }

                coVerify(exactly = 1) { appleUseCase(credential) }
                coVerify(exactly = 0) { googleUseCase(any()) }
            }
        }

        test("실패 Effect를 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val credential = Arb.googleCredentialIdToken().next()
                val googleUseCase = mockk<SignInWithGoogleUseCase>()
                val appleUseCase = mockk<SignInWithAppleUseCase>()
                coEvery { googleUseCase(any()) } returns Result.failure(IllegalStateException("Sign-in failed"))
                val viewModel = loginHomeViewModel(googleUseCase, appleUseCase)

                viewModel.effect.test {
                    viewModel.signInWithGoogle(credential)
                    advanceUntilIdle()

                    awaitItem() shouldBe LoginHomeEffect.SignInFailed
                    expectNoEvents()
                }

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("Apple 로그인 실패 Effect를 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val credential = Arb.appleCredential().next()
                val googleUseCase = mockk<SignInWithGoogleUseCase>()
                val appleUseCase = mockk<SignInWithAppleUseCase>()
                coEvery { appleUseCase(any()) } returns Result.failure(IllegalStateException("Sign-in failed"))
                val viewModel = loginHomeViewModel(googleUseCase, appleUseCase)

                viewModel.effect.test {
                    viewModel.signInWithApple(credential)
                    advanceUntilIdle()

                    awaitItem() shouldBe LoginHomeEffect.SignInFailed
                    expectNoEvents()
                }

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-09801d23-391b-4709-89d7-495cd0a73bcb 같은 수단으로 실패 후 다시 로그인할 수 있다") {
            runTest(mainDispatcher) {
                val firstCredential = Arb.googleCredentialIdToken().next()
                val secondCredential = Arb.googleCredentialAuthorizationCode().next()
                val googleUseCase = mockk<SignInWithGoogleUseCase>()
                val appleUseCase = mockk<SignInWithAppleUseCase>()
                coEvery { googleUseCase(firstCredential) } returns Result.failure(IllegalStateException("Sign-in failed"))
                coEvery { googleUseCase(secondCredential) } returns Result.success(Unit)
                val viewModel = loginHomeViewModel(googleUseCase, appleUseCase)

                viewModel.effect.test {
                    viewModel.signInWithGoogle(firstCredential)
                    advanceUntilIdle()

                    awaitItem() shouldBe LoginHomeEffect.SignInFailed
                    viewModel.uiState.value.isInProgress
                        .shouldBeFalse()

                    viewModel.signInWithGoogle(secondCredential)
                    advanceUntilIdle()

                    awaitItem() shouldBe LoginHomeEffect.SignInSucceeded
                    expectNoEvents()
                }

                coVerifyOrder {
                    googleUseCase(firstCredential)
                    googleUseCase(secondCredential)
                }
            }
        }

        test("TC-09801d23-391b-4709-89d7-495cd0a73bcb 다른 수단으로 실패 후 다시 로그인할 수 있다") {
            runTest(mainDispatcher) {
                val googleCredential = Arb.googleCredentialIdToken().next()
                val appleCredential = Arb.appleCredential().next()
                val googleUseCase = mockk<SignInWithGoogleUseCase>()
                val appleUseCase = mockk<SignInWithAppleUseCase>()
                coEvery { googleUseCase(googleCredential) } returns Result.failure(IllegalStateException("Sign-in failed"))
                coEvery { appleUseCase(appleCredential) } returns Result.success(Unit)
                val viewModel = loginHomeViewModel(googleUseCase, appleUseCase)

                viewModel.effect.test {
                    viewModel.signInWithGoogle(googleCredential)
                    advanceUntilIdle()

                    awaitItem() shouldBe LoginHomeEffect.SignInFailed
                    viewModel.uiState.value.isInProgress
                        .shouldBeFalse()

                    viewModel.signInWithApple(appleCredential)
                    advanceUntilIdle()

                    awaitItem() shouldBe LoginHomeEffect.SignInSucceeded
                    expectNoEvents()
                }

                coVerifyOrder {
                    googleUseCase(googleCredential)
                    appleUseCase(appleCredential)
                }
            }
        }

        context("TC-6920889b-7b94-4800-90b3-92536d07ec18 Login 화면에 다시 들어오면 로그인 수단 선택 대기 상태에서 시작한다") {
            listOf<Pair<String, (SignInWithGoogleUseCase, GoogleCredential) -> Unit>>(
                "앱 로그인 처리 중" to { useCase, credential ->
                    coEvery { useCase(credential) } coAnswers { CompletableDeferred<Result<Unit>>().await() }
                },
                "앱 로그인 실패" to { useCase, credential ->
                    coEvery { useCase(credential) } returns Result.failure(IllegalStateException())
                },
            ).forEach { (name, arrange) ->
                test(name) {
                    runTest(mainDispatcher) {
                        val credential = Arb.googleCredentialIdToken().next()
                        val googleUseCase = mockk<SignInWithGoogleUseCase>()
                        val appleUseCase = mockk<SignInWithAppleUseCase>()
                        arrange(googleUseCase, credential)
                        val previousViewModel = loginHomeViewModel(googleUseCase, appleUseCase)
                        previousViewModel.signInWithGoogle(credential)
                        runCurrent()

                        val viewModel = loginHomeViewModel(googleUseCase, appleUseCase)

                        viewModel.uiState.value.isInProgress
                            .shouldBeFalse()
                        viewModel.effect.test {
                            expectNoEvents()
                        }
                    }
                }
            }
        }

        test("TC-9cb265a3-39f1-4db0-85e6-b8cb5538ee85 앱 로그인 처리 중 화면을 떠나 ViewModel이 정리되면 시도를 중단하고 안내하지 않는다") {
            runTest(mainDispatcher) {
                val credential = Arb.googleCredentialIdToken().next()
                val cancelled = CompletableDeferred<Unit>()
                val googleUseCase = mockk<SignInWithGoogleUseCase>()
                val appleUseCase = mockk<SignInWithAppleUseCase>()
                coEvery { googleUseCase(any()) } coAnswers {
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
                        factory = viewModelFactory { initializer { loginHomeViewModel(googleUseCase, appleUseCase) } },
                    )[LoginHomeViewModel::class]

                viewModel.effect.test {
                    viewModel.signInWithGoogle(credential)
                    runCurrent()

                    store.clear()
                    advanceUntilIdle()

                    cancelled.isCompleted shouldBe true
                    expectNoEvents()
                }
            }
        }

        test("로그인이 취소되면 진행 상태를 해제하고 다시 로그인할 수 있다") {
            runTest(mainDispatcher) {
                val firstCredential = Arb.googleCredentialIdToken().next()
                val secondCredential = Arb.googleCredentialAuthorizationCode().next()
                val googleUseCase = mockk<SignInWithGoogleUseCase>()
                val appleUseCase = mockk<SignInWithAppleUseCase>()
                coEvery { googleUseCase(firstCredential) } throws CancellationException()
                coEvery { googleUseCase(secondCredential) } returns Result.success(Unit)
                val viewModel = loginHomeViewModel(googleUseCase, appleUseCase)

                viewModel.signInWithGoogle(firstCredential)
                advanceUntilIdle()

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()

                viewModel.signInWithGoogle(secondCredential)
                advanceUntilIdle()

                coVerifyOrder {
                    googleUseCase(firstCredential)
                    googleUseCase(secondCredential)
                }
            }
        }
    }

    public companion object {
        private fun loginHomeViewModel(
            signInWithGoogleUseCase: SignInWithGoogleUseCase,
            signInWithAppleUseCase: SignInWithAppleUseCase,
        ): LoginHomeViewModel =
            LoginHomeViewModel(
                signInWithGoogleUseCase = signInWithGoogleUseCase,
                signInWithAppleUseCase = signInWithAppleUseCase,
            )
    }
}
