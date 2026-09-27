@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.login.ui.home

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.authentication.AppleCredential
import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential
import io.github.taetae98coding.diary.domain.account.usecase.SignInWithAppleUseCase
import io.github.taetae98coding.diary.domain.account.usecase.SignInWithGoogleUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

        test("TC-LOGIN-FEATURE-009 로그인 진행 상태를 전환한다") {
            runTest(mainDispatcher) {
                val credential = fixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
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

        test("TC-LOGIN-FEATURE-009 Apple 로그인 진행 상태를 전환한다") {
            runTest(mainDispatcher) {
                val credential = fixtureMonkey.giveMeOne<AppleCredential>()
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

        test("TC-LOGIN-FEATURE-010 진행 중 같은 수단의 추가 요청 무시") {
            runTest(mainDispatcher) {
                val firstCredential = fixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
                val secondCredential = fixtureMonkey.giveMeOne<GoogleCredential.AuthorizationCode>()
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

        test("TC-LOGIN-FEATURE-010 진행 중 다른 수단의 추가 요청 무시") {
            runTest(mainDispatcher) {
                val googleCredential = fixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
                val appleCredential = fixtureMonkey.giveMeOne<AppleCredential>()
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
                val credential = fixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
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
                val credential = fixtureMonkey.giveMeOne<AppleCredential>()
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
                val credential = fixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
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
                val credential = fixtureMonkey.giveMeOne<AppleCredential>()
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

        test("TC-LOGIN-FEATURE-015 같은 수단으로 실패 후 다시 로그인할 수 있다") {
            runTest(mainDispatcher) {
                val firstCredential = fixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
                val secondCredential = fixtureMonkey.giveMeOne<GoogleCredential.AuthorizationCode>()
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

        test("TC-LOGIN-FEATURE-015 다른 수단으로 실패 후 다시 로그인할 수 있다") {
            runTest(mainDispatcher) {
                val googleCredential = fixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
                val appleCredential = fixtureMonkey.giveMeOne<AppleCredential>()
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

        context("TC-LOGIN-FEATURE-026 Login 화면에 다시 들어오면 로그인 수단 선택 대기 상태에서 시작한다") {
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
                        val credential = fixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
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

        test("로그인이 취소되면 진행 상태를 해제하고 다시 로그인할 수 있다") {
            runTest(mainDispatcher) {
                val firstCredential = fixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
                val secondCredential = fixtureMonkey.giveMeOne<GoogleCredential.AuthorizationCode>()
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
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

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
