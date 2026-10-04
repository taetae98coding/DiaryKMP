@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.core.sync

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
import kotlinx.coroutines.CompletableDeferred
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

class SyncRefreshViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        // 화면마다 같은 동작을 문서화한 케이스를 이 ViewModel 하나가 함께 만족한다.
        listOf(
            "TC-SYNC-REFRESH-FEATURE-001",
            "TC-CONTACT-HOME-FEATURE-017",
            "TC-PLACE-HOME-FEATURE-029",
            "TC-PLAYLIST-HOME-FEATURE-008",
            "TC-QR-HOME-FEATURE-023",
            "TC-WEB-HOME-FEATURE-013",
        ).forEach { id ->
            test("$id 새로고침하면 진행을 표시할 동기화를 한 번 요청한다") {
                runTest(mainDispatcher) {
                    val requestSyncUseCase = mockk<RequestSyncUseCase>()
                    coEvery { requestSyncUseCase(parameter = SyncTrigger.USER_REQUESTED) } returns Result.success(Unit)
                    val viewModel = viewModel(requestSyncUseCase = requestSyncUseCase)

                    viewModel.refresh()
                    advanceUntilIdle()

                    coVerify(exactly = 1) { requestSyncUseCase(parameter = SyncTrigger.USER_REQUESTED) }
                }
            }
        }

        listOf(
            "TC-SYNC-REFRESH-FEATURE-002",
            "TC-CONTACT-HOME-FEATURE-019",
            "TC-PLACE-HOME-FEATURE-030",
            "TC-PLAYLIST-HOME-FEATURE-009",
            "TC-QR-HOME-FEATURE-024",
            "TC-WEB-HOME-FEATURE-015",
        ).forEach { id ->
            test("$id 동기화가 실행되는 동안 진행을 노출하고 끝나면 해제한다") {
                runTest(mainDispatcher) {
                    val isRefreshingFlow = MutableStateFlow(Result.success(false))
                    val viewModel = viewModel(getProgressReportedUseCase = progressReportedUseCase(isRefreshingFlow = isRefreshingFlow))

                    viewModel.uiState.test {
                        awaitItem() shouldBe SyncRefreshUiState()

                        isRefreshingFlow.value = Result.success(true)
                        awaitItem() shouldBe SyncRefreshUiState(isRefreshing = true)

                        isRefreshingFlow.value = Result.success(false)
                        awaitItem() shouldBe SyncRefreshUiState(isRefreshing = false)
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        listOf(
            "TC-CONTACT-HOME-DATA-004",
            "TC-PLAYLIST-HOME-DATA-003",
            "TC-QR-HOME-DATA-003",
            "TC-WEB-DETAIL-DATA-003",
            "TC-WEB-HOME-DATA-004",
        ).forEach { id ->
            test("$id 화면 상태만 관찰하면 서버 동기화를 요청하지 않는다") {
                runTest(mainDispatcher) {
                    val requestSyncUseCase = mockk<RequestSyncUseCase>()
                    val viewModel = viewModel(requestSyncUseCase = requestSyncUseCase)

                    viewModel.uiState.test {
                        awaitItem() shouldBe SyncRefreshUiState()
                        advanceUntilIdle()

                        cancelAndIgnoreRemainingEvents()
                    }

                    coVerify(exactly = 0) { requestSyncUseCase(parameter = any()) }
                }
            }
        }

        test("동기화 진행 여부 조회가 실패하면 진행을 표시하지 않는다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(
                        getProgressReportedUseCase =
                            progressReportedUseCase(
                                isRefreshingFlow = flowOf(Result.failure(IllegalStateException("sync state error"))),
                            ),
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe SyncRefreshUiState()
                    advanceUntilIdle()

                    expectNoEvents()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("요청이 끝나기 전에 다시 새로고침하면 동기화를 한 번만 요청한다") {
            runTest(mainDispatcher) {
                val requestSyncUseCase = mockk<RequestSyncUseCase>()
                val pending = CompletableDeferred<Result<Unit>>()
                coEvery { requestSyncUseCase(parameter = SyncTrigger.USER_REQUESTED) } coAnswers { pending.await() }
                val viewModel = viewModel(requestSyncUseCase = requestSyncUseCase)

                viewModel.refresh()
                advanceUntilIdle()
                viewModel.refresh()
                advanceUntilIdle()

                coVerify(exactly = 1) { requestSyncUseCase(parameter = SyncTrigger.USER_REQUESTED) }

                pending.complete(Result.success(Unit))
                advanceUntilIdle()
            }
        }

        test("요청이 끝난 뒤 다시 새로고침하면 동기화를 다시 요청한다") {
            runTest(mainDispatcher) {
                val requestSyncUseCase = mockk<RequestSyncUseCase>()
                coEvery { requestSyncUseCase(parameter = SyncTrigger.USER_REQUESTED) } returns Result.success(Unit)
                val viewModel = viewModel(requestSyncUseCase = requestSyncUseCase)

                viewModel.refresh()
                advanceUntilIdle()
                viewModel.refresh()
                advanceUntilIdle()

                coVerify(exactly = 2) { requestSyncUseCase(parameter = SyncTrigger.USER_REQUESTED) }
            }
        }
    }

    private companion object {
        private fun viewModel(
            getProgressReportedUseCase: GetProgressReportedUseCase = progressReportedUseCase(),
            requestSyncUseCase: RequestSyncUseCase = mockk(),
        ): SyncRefreshViewModel =
            SyncRefreshViewModel(
                getProgressReportedUseCase = getProgressReportedUseCase,
                requestSyncUseCase = requestSyncUseCase,
            )

        private fun progressReportedUseCase(isRefreshingFlow: Flow<Result<Boolean>> = flowOf(Result.success(false))): GetProgressReportedUseCase {
            val getProgressReportedUseCase = mockk<GetProgressReportedUseCase>()
            every { getProgressReportedUseCase(parameter = Unit) } returns isRefreshingFlow

            return getProgressReportedUseCase
        }
    }
}
