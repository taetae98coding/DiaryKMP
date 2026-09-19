@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.playlist.ui.home

import app.cash.turbine.test
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.GetProgressReportedUseCase
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class PlaylistHomeSyncViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-PLAYLIST-HOME-FEATURE-008 새로고침하면 진행을 표시할 동기화를 요청한다") {
            runTest(mainDispatcher) {
                val requestSyncUseCase = mockk<RequestSyncUseCase>()
                coEvery { requestSyncUseCase(parameter = SyncTrigger.USER_REQUESTED) } returns Result.success(Unit)
                val viewModel = viewModel(requestSyncUseCase = requestSyncUseCase)

                viewModel.refresh()
                advanceUntilIdle()

                coVerify(exactly = 1) { requestSyncUseCase(parameter = SyncTrigger.USER_REQUESTED) }
            }
        }

        test("TC-PLAYLIST-HOME-DATA-003 화면 상태만 관찰하면 서버 동기화를 요청하지 않는다") {
            runTest(mainDispatcher) {
                val requestSyncUseCase = mockk<RequestSyncUseCase>()
                val viewModel = viewModel(requestSyncUseCase = requestSyncUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe PlaylistHomeUiState()
                    advanceUntilIdle()

                    cancelAndIgnoreRemainingEvents()
                }

                coVerify(exactly = 0) { requestSyncUseCase(parameter = any()) }
            }
        }

        test("TC-PLAYLIST-HOME-FEATURE-009 동기화 진행 여부를 화면에 노출한다") {
            runTest(mainDispatcher) {
                val isRefreshingFlow = MutableStateFlow(Result.success(false))
                val viewModel = viewModel(getProgressReportedUseCase = syncRefreshingUseCase(isRefreshingFlow = isRefreshingFlow))

                viewModel.uiState.test {
                    awaitItem() shouldBe PlaylistHomeUiState()

                    isRefreshingFlow.value = Result.success(true)
                    awaitItem() shouldBe PlaylistHomeUiState(isRefreshing = true)

                    isRefreshingFlow.value = Result.success(false)
                    awaitItem() shouldBe PlaylistHomeUiState(isRefreshing = false)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("동기화 진행 여부 조회가 실패하면 진행을 표시하지 않는다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(
                        getProgressReportedUseCase =
                            syncRefreshingUseCase(
                                isRefreshingFlow = flowOf(Result.failure(IllegalStateException("sync state error"))),
                            ),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe PlaylistHomeUiState()
                    advanceUntilIdle()

                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    private companion object {
        private fun viewModel(
            getProgressReportedUseCase: GetProgressReportedUseCase = syncRefreshingUseCase(),
            requestSyncUseCase: RequestSyncUseCase = mockk(),
        ): PlaylistHomeSyncViewModel =
            PlaylistHomeSyncViewModel(
                getProgressReportedUseCase = getProgressReportedUseCase,
                requestSyncUseCase = requestSyncUseCase,
            )

        private fun syncRefreshingUseCase(isRefreshingFlow: Flow<Result<Boolean>> = flowOf(Result.success(false))): GetProgressReportedUseCase {
            val getProgressReportedUseCase = mockk<GetProgressReportedUseCase>()
            every { getProgressReportedUseCase(parameter = Unit) } returns isRefreshingFlow

            return getProgressReportedUseCase
        }
    }
}
