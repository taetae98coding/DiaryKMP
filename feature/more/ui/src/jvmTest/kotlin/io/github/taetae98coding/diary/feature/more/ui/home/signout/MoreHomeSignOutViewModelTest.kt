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

        test("TC-28c9f8de-19c1-41d7-ad20-9f061b5f6e83 TC-a9e03db2-b461-4351-8506-d90fa5fe6a90 업로드 대기 항목이 없으면 확인 없이 로그아웃한다") {
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

        test("TC-3d7dc771-5c19-4013-ba60-b2a1f705a149 업로드 대기 항목이 있으면 확인 다이얼로그를 열고 로그아웃하지 않는다") {
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

        test("TC-6a0c54d2-cdc9-4df9-ac7a-3dc236c0e4f6 확인 다이얼로그에서 로그아웃을 고르면 다이얼로그를 닫고 로그아웃한다") {
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

        test("TC-c4f73ebf-449e-43f0-a3f9-1d87743a56cd 확인 다이얼로그에서 취소를 고르면 다이얼로그를 닫고 로그아웃하지 않는다") {
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

        test("TC-6ce82c08-c4cd-48c6-a72f-fa200fa273f0 다이얼로그가 열린 뒤 업로드 대기 항목이 해제되어도 다이얼로그를 닫지 않는다") {
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

        test("TC-25eee4be-9eb9-4790-80a8-abdbadd44c4e 시스템이 앱을 종료했다가 되살려도 다이얼로그가 열린 상태를 유지한다") {
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

        test("TC-542fb42d-3e23-498b-bf85-e1c82d7a3419 닫은 다이얼로그는 시스템이 앱을 종료했다가 되살려도 다시 열리지 않는다") {
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

        test("TC-4b2a218b-2732-486b-a42f-e1ebb39267f1 업로드 대기 항목을 확인하지 못하면 확인 없이 로그아웃한다") {
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

        test("TC-0acd3802-a869-448b-b008-6ec3f7d8e7e9 로그아웃에 실패하면 안내 없이 계정 표시를 유지한다") {
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

        test("TC-4ee2634b-a9fe-41a7-8f9c-5db073eae4fe 로그아웃 동작을 선택하면 업로드 대기 여부를 한 번 조회한다") {
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

        test("TC-ed16b7dd-6081-4893-a56f-9a2301d0fbe9 업로드 대기 항목을 확인하는 동안 진행 상태와 다이얼로그를 표시하지 않는다") {
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

        test("TC-ed16b7dd-6081-4893-a56f-9a2301d0fbe9 로그아웃을 처리하는 동안 진행 상태를 표시하지 않고 계정 상태를 유지한다") {
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
            test("TC-f8af2b1d-f38e-4e1a-abbc-802d215f2f63 업로드 대기 항목이 ${if (hasPending) "있을" else "없을"} 때 처리 중 다시 선택해도 진행 중인 처리만 이어진다") {
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

                    verify(exactly = 1) { findSyncPendingUseCase(Unit) }
                    viewModel.uiState.value shouldBe MoreHomeSignOutUiState(isConfirmVisible = hasPending)
                    coVerify(exactly = if (hasPending) 0 else 1) { signOutUseCase(Unit) }
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
