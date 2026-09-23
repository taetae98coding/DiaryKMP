@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.more.ui.home.signout

import app.cash.turbine.test
import io.github.taetae98coding.diary.domain.account.usecase.SignOutUseCase
import io.github.taetae98coding.diary.domain.sync.usecase.FindSyncPendingUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class MoreHomeSignOutViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-MORE-HOME-FEATURE-007 TC-MORE-HOME-DATA-001 업로드 대기 항목이 없으면 확인 없이 로그아웃한다") {
            runTest(mainDispatcher) {
                val signOutUseCase = signOutUseCase()
                val viewModel = viewModel(hasPending = false, signOutUseCase = signOutUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe MoreHomeSignOutUiState()

                    viewModel.signOut()
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 1) { signOutUseCase(Unit) }
            }
        }

        test("TC-MORE-HOME-FEATURE-026 업로드 대기 항목이 있으면 확인 다이얼로그를 열고 로그아웃하지 않는다") {
            runTest(mainDispatcher) {
                val signOutUseCase = signOutUseCase()
                val viewModel = viewModel(hasPending = true, signOutUseCase = signOutUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe MoreHomeSignOutUiState()

                    viewModel.signOut()
                    advanceUntilIdle()

                    awaitItem() shouldBe MoreHomeSignOutUiState(isConfirmVisible = true)
                }

                coVerify(exactly = 0) { signOutUseCase(Unit) }
            }
        }

        test("TC-MORE-HOME-FEATURE-027 확인 다이얼로그에서 로그아웃을 고르면 다이얼로그를 닫고 로그아웃한다") {
            runTest(mainDispatcher) {
                val signOutUseCase = signOutUseCase()
                val viewModel = viewModel(hasPending = true, signOutUseCase = signOutUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe MoreHomeSignOutUiState()

                    viewModel.signOut()
                    advanceUntilIdle()
                    awaitItem() shouldBe MoreHomeSignOutUiState(isConfirmVisible = true)

                    viewModel.confirmSignOut()
                    advanceUntilIdle()
                    awaitItem() shouldBe MoreHomeSignOutUiState(isConfirmVisible = false)
                }

                coVerify(exactly = 1) { signOutUseCase(Unit) }
            }
        }

        test("TC-MORE-HOME-FEATURE-028 확인 다이얼로그에서 취소를 고르면 다이얼로그를 닫고 로그아웃하지 않는다") {
            runTest(mainDispatcher) {
                val signOutUseCase = signOutUseCase()
                val viewModel = viewModel(hasPending = true, signOutUseCase = signOutUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe MoreHomeSignOutUiState()

                    viewModel.signOut()
                    advanceUntilIdle()
                    awaitItem() shouldBe MoreHomeSignOutUiState(isConfirmVisible = true)

                    viewModel.cancelSignOut()
                    advanceUntilIdle()
                    awaitItem() shouldBe MoreHomeSignOutUiState(isConfirmVisible = false)
                }

                coVerify(exactly = 0) { signOutUseCase(Unit) }
            }
        }

        test("TC-MORE-HOME-DOMAIN-008 다이얼로그가 열린 뒤 업로드 대기 항목이 해제되어도 다이얼로그를 닫지 않는다") {
            runTest(mainDispatcher) {
                val signOutUseCase = signOutUseCase()
                val findSyncPendingUseCase = mockk<FindSyncPendingUseCase>()
                every { findSyncPendingUseCase(Unit) } returns flowOf(Result.success(true))
                val viewModel =
                    MoreHomeSignOutViewModel(
                        findSyncPendingUseCase = findSyncPendingUseCase,
                        signOutUseCase = signOutUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MoreHomeSignOutUiState()

                    viewModel.signOut()
                    advanceUntilIdle()
                    awaitItem() shouldBe MoreHomeSignOutUiState(isConfirmVisible = true)

                    every { findSyncPendingUseCase(Unit) } returns flowOf(Result.success(false))
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 0) { signOutUseCase(Unit) }
            }
        }

        test("TC-MORE-HOME-DATA-003 로그아웃 동작을 선택하면 업로드 대기 여부를 한 번 조회한다") {
            runTest(mainDispatcher) {
                val findSyncPendingUseCase = mockk<FindSyncPendingUseCase>()
                every { findSyncPendingUseCase(Unit) } returns flowOf(Result.success(true))
                val viewModel =
                    MoreHomeSignOutViewModel(
                        findSyncPendingUseCase = findSyncPendingUseCase,
                        signOutUseCase = signOutUseCase(),
                    )

                viewModel.signOut()
                advanceUntilIdle()

                verify(exactly = 1) { findSyncPendingUseCase(Unit) }
            }
        }

        test("확인 다이얼로그를 거친 로그아웃은 업로드 대기 여부를 다시 조회하지 않는다") {
            runTest(mainDispatcher) {
                val findSyncPendingUseCase = mockk<FindSyncPendingUseCase>()
                every { findSyncPendingUseCase(Unit) } returns flowOf(Result.success(true))
                val viewModel =
                    MoreHomeSignOutViewModel(
                        findSyncPendingUseCase = findSyncPendingUseCase,
                        signOutUseCase = signOutUseCase(),
                    )

                viewModel.signOut()
                advanceUntilIdle()
                viewModel.confirmSignOut()
                advanceUntilIdle()

                verify(exactly = 1) { findSyncPendingUseCase(Unit) }
            }
        }
    }

    public companion object {
        private fun signOutUseCase(): SignOutUseCase {
            val signOutUseCase = mockk<SignOutUseCase>()
            coEvery { signOutUseCase(Unit) } returns Result.success(Unit)
            return signOutUseCase
        }

        private fun viewModel(
            hasPending: Boolean,
            signOutUseCase: SignOutUseCase,
        ): MoreHomeSignOutViewModel {
            val findSyncPendingUseCase = mockk<FindSyncPendingUseCase>()
            every { findSyncPendingUseCase(Unit) } returns flowOf(Result.success(hasPending))

            return MoreHomeSignOutViewModel(
                findSyncPendingUseCase = findSyncPendingUseCase,
                signOutUseCase = signOutUseCase,
            )
        }
    }
}
