@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.more.ui.home.signout

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.account.usecase.SignOutUseCase
import io.github.taetae98coding.diary.domain.sync.usecase.FindSyncPendingUseCase
import io.github.taetae98coding.diary.feature.more.ui.home.account.MoreHomeAccountUiState
import io.github.taetae98coding.diary.feature.more.ui.home.account.MoreHomeAccountViewModel
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

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
                val syncPending = MutableStateFlow(Result.success(true))
                val findSyncPendingUseCase = mockk<FindSyncPendingUseCase>()
                every { findSyncPendingUseCase(Unit) } returns syncPending
                val viewModel =
                    MoreHomeSignOutViewModel(
                        savedStateHandle = SavedStateHandle(),
                        findSyncPendingUseCase = findSyncPendingUseCase,
                        signOutUseCase = signOutUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MoreHomeSignOutUiState()

                    viewModel.signOut()
                    advanceUntilIdle()
                    awaitItem() shouldBe MoreHomeSignOutUiState(isConfirmVisible = true)

                    syncPending.value = Result.success(false)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 0) { signOutUseCase(Unit) }
            }
        }

        test("TC-MORE-HOME-DOMAIN-009 시스템이 앱을 종료했다가 되살려도 다이얼로그가 열린 상태를 유지한다") {
            runTest(mainDispatcher) {
                val savedStateHandle = SavedStateHandle()
                val viewModel =
                    viewModel(hasPending = true, signOutUseCase = signOutUseCase(), savedStateHandle = savedStateHandle)
                viewModel.signOut()
                advanceUntilIdle()
                viewModel.uiState.value shouldBe MoreHomeSignOutUiState(isConfirmVisible = true)

                val signOutUseCase = signOutUseCase()
                val restoredViewModel =
                    viewModel(
                        hasPending = false,
                        signOutUseCase = signOutUseCase,
                        savedStateHandle = savedStateHandle.restored(),
                    )

                restoredViewModel.uiState.value shouldBe MoreHomeSignOutUiState(isConfirmVisible = true)
                coVerify(exactly = 0) { signOutUseCase(Unit) }
            }
        }

        test("TC-MORE-HOME-DOMAIN-015 닫은 다이얼로그는 시스템이 앱을 종료했다가 되살려도 다시 열리지 않는다") {
            runTest(mainDispatcher) {
                val savedStateHandle = SavedStateHandle()
                val viewModel =
                    viewModel(hasPending = true, signOutUseCase = signOutUseCase(), savedStateHandle = savedStateHandle)
                viewModel.signOut()
                advanceUntilIdle()
                viewModel.cancelSignOut()

                val restoredViewModel =
                    viewModel(
                        hasPending = true,
                        signOutUseCase = signOutUseCase(),
                        savedStateHandle = savedStateHandle.restored(),
                    )

                restoredViewModel.uiState.value shouldBe MoreHomeSignOutUiState(isConfirmVisible = false)
            }
        }

        test("TC-MORE-HOME-FEATURE-034 업로드 대기 항목을 확인하지 못하면 확인 없이 로그아웃한다") {
            runTest(mainDispatcher) {
                val signOutUseCase = signOutUseCase()
                val findSyncPendingUseCase = mockk<FindSyncPendingUseCase>()
                every { findSyncPendingUseCase(Unit) } returns
                    flowOf(Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>())))
                val viewModel =
                    MoreHomeSignOutViewModel(
                        savedStateHandle = SavedStateHandle(),
                        findSyncPendingUseCase = findSyncPendingUseCase,
                        signOutUseCase = signOutUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MoreHomeSignOutUiState()

                    viewModel.signOut()
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 1) { signOutUseCase(Unit) }
            }
        }

        test("TC-MORE-HOME-FEATURE-035 로그아웃에 실패하면 안내 없이 계정 표시를 유지한다") {
            runTest(mainDispatcher) {
                val email = "diary-" + fixtureMonkey.giveMeOne<String>()
                val profileImage = fixtureMonkey.giveMeOne<String>()
                val getAccountUseCase = mockk<GetAccountUseCase>()
                every { getAccountUseCase(Unit) } returns
                    flowOf(
                        Result.success<Account>(
                            Account.User(
                                id = fixtureMonkey.giveMeOne<Uuid>(),
                                profileImage = profileImage,
                                email = email,
                                isSessionValid = fixtureMonkey.giveMeOne<Boolean>(),
                            ),
                        ),
                    )
                val accountViewModel = MoreHomeAccountViewModel(getAccountUseCase = getAccountUseCase)
                val signOutUseCase = mockk<SignOutUseCase>()
                coEvery { signOutUseCase(Unit) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = viewModel(hasPending = false, signOutUseCase = signOutUseCase)

                accountViewModel.uiState.test {
                    awaitItem() shouldBe MoreHomeAccountUiState.Loading
                    awaitItem() shouldBe MoreHomeAccountUiState.User(profileImage = profileImage, email = email)

                    viewModel.signOut()
                    advanceUntilIdle()

                    expectNoEvents()
                }

                viewModel.uiState.value shouldBe MoreHomeSignOutUiState()
                coVerify(exactly = 1) { signOutUseCase(Unit) }
            }
        }

        test("TC-MORE-HOME-DATA-003 로그아웃 동작을 선택하면 업로드 대기 여부를 한 번 조회한다") {
            runTest(mainDispatcher) {
                val findSyncPendingUseCase = mockk<FindSyncPendingUseCase>()
                every { findSyncPendingUseCase(Unit) } returns flowOf(Result.success(true))
                val viewModel =
                    MoreHomeSignOutViewModel(
                        savedStateHandle = SavedStateHandle(),
                        findSyncPendingUseCase = findSyncPendingUseCase,
                        signOutUseCase = signOutUseCase(),
                    )

                viewModel.signOut()
                advanceUntilIdle()

                verify(exactly = 1) { findSyncPendingUseCase(Unit) }
            }
        }

        test("TC-MORE-HOME-FEATURE-039 업로드 대기 항목을 확인하는 동안 진행 상태와 다이얼로그를 표시하지 않는다") {
            runTest(mainDispatcher) {
                val findSyncPendingUseCase = mockk<FindSyncPendingUseCase>()
                every { findSyncPendingUseCase(Unit) } returns MutableSharedFlow()
                val viewModel =
                    MoreHomeSignOutViewModel(
                        savedStateHandle = SavedStateHandle(),
                        findSyncPendingUseCase = findSyncPendingUseCase,
                        signOutUseCase = signOutUseCase(),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe MoreHomeSignOutUiState()

                    viewModel.signOut()
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-MORE-HOME-FEATURE-039 로그아웃을 처리하는 동안 진행 상태를 표시하지 않고 계정 상태를 유지한다") {
            runTest(mainDispatcher) {
                val email = "diary-" + fixtureMonkey.giveMeOne<String>()
                val profileImage = fixtureMonkey.giveMeOne<String>()
                val getAccountUseCase = mockk<GetAccountUseCase>()
                every { getAccountUseCase(Unit) } returns
                    flowOf(
                        Result.success<Account>(
                            Account.User(
                                id = fixtureMonkey.giveMeOne<Uuid>(),
                                profileImage = profileImage,
                                email = email,
                                isSessionValid = fixtureMonkey.giveMeOne<Boolean>(),
                            ),
                        ),
                    )
                val accountViewModel = MoreHomeAccountViewModel(getAccountUseCase = getAccountUseCase)
                val signOutUseCase = mockk<SignOutUseCase>()
                coEvery { signOutUseCase(Unit) } coAnswers { awaitCancellation() }
                val viewModel = viewModel(hasPending = false, signOutUseCase = signOutUseCase)

                accountViewModel.uiState.test {
                    awaitItem() shouldBe MoreHomeAccountUiState.Loading
                    awaitItem() shouldBe MoreHomeAccountUiState.User(profileImage = profileImage, email = email)

                    viewModel.signOut()
                    advanceUntilIdle()

                    expectNoEvents()
                }

                viewModel.uiState.value shouldBe MoreHomeSignOutUiState()
                coVerify(exactly = 1) { signOutUseCase(Unit) }
            }
        }

        listOf(true, false).forEach { hasPending ->
            test("TC-MORE-HOME-FEATURE-040 업로드 대기 항목이 ${if (hasPending) "있을" else "없을"} 때 처리 중 다시 선택하면 선택할 때마다 같은 기준으로 처리한다") {
                runTest(mainDispatcher) {
                    val pendingFlow = MutableSharedFlow<Result<Boolean>>(replay = 1)
                    val findSyncPendingUseCase = mockk<FindSyncPendingUseCase>()
                    every { findSyncPendingUseCase(Unit) } returns pendingFlow
                    val signOutUseCase = signOutUseCase()
                    val viewModel =
                        MoreHomeSignOutViewModel(
                            savedStateHandle = SavedStateHandle(),
                            findSyncPendingUseCase = findSyncPendingUseCase,
                            signOutUseCase = signOutUseCase,
                        )

                    viewModel.signOut()
                    viewModel.signOut()
                    advanceUntilIdle()
                    pendingFlow.emit(Result.success(hasPending))
                    advanceUntilIdle()

                    verify(exactly = 2) { findSyncPendingUseCase(Unit) }
                    viewModel.uiState.value shouldBe MoreHomeSignOutUiState(isConfirmVisible = hasPending)
                    coVerify(exactly = if (hasPending) 0 else 2) { signOutUseCase(Unit) }
                }
            }
        }

        test("확인 다이얼로그를 거친 로그아웃은 업로드 대기 여부를 다시 조회하지 않는다") {
            runTest(mainDispatcher) {
                val findSyncPendingUseCase = mockk<FindSyncPendingUseCase>()
                every { findSyncPendingUseCase(Unit) } returns flowOf(Result.success(true))
                val viewModel =
                    MoreHomeSignOutViewModel(
                        savedStateHandle = SavedStateHandle(),
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
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        private fun signOutUseCase(): SignOutUseCase {
            val signOutUseCase = mockk<SignOutUseCase>()
            coEvery { signOutUseCase(Unit) } returns Result.success(Unit)
            return signOutUseCase
        }

        // 시스템이 앱을 종료할 때 저장되는 값만 새 저장소로 옮겨 되살린 상태를 만든다.
        private fun SavedStateHandle.restored(): SavedStateHandle = SavedStateHandle(initialState = keys().associateWith { key -> get<Any?>(key) })

        private fun viewModel(
            hasPending: Boolean,
            signOutUseCase: SignOutUseCase,
            savedStateHandle: SavedStateHandle = SavedStateHandle(),
        ): MoreHomeSignOutViewModel {
            val findSyncPendingUseCase = mockk<FindSyncPendingUseCase>()
            every { findSyncPendingUseCase(Unit) } returns flowOf(Result.success(hasPending))

            return MoreHomeSignOutViewModel(
                savedStateHandle = savedStateHandle,
                findSyncPendingUseCase = findSyncPendingUseCase,
                signOutUseCase = signOutUseCase,
            )
        }
    }
}
