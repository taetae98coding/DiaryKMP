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
            "TC-221754fa-d814-4c80-9c41-c0bdce86574f",
            "TC-a082975e-5b82-4140-bf35-04ac6a7511db",
            "TC-25de4f78-b943-4194-baee-929590a27bba",
            "TC-b33a6b98-eb07-418b-9686-9ae2b3850279",
            "TC-baec8bc1-1a9a-49bf-b47d-e1f4212413f4",
            "TC-a0d2d609-161f-429e-9ca9-5316a5689869",
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
            "TC-fb7d9b53-80c5-425b-b1c3-06f21ac30643",
            "TC-f1366e80-eba2-4bb8-8715-9b2f3bf56840",
            "TC-193cad63-e621-481e-92df-28cc9a0fc36e",
            "TC-93a2c915-4de7-40d6-878c-1b42d43310dd",
            "TC-83b267ce-2bdf-4f78-a9dd-787149b0a5da",
            "TC-e27e3473-1d9f-486a-9951-efd7b309ae1f",
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
            "TC-5cbaec82-618e-408a-91e1-5500867cf2cb",
            "TC-d55111f9-8681-476a-bb13-a7a14edecf64",
            "TC-359a26bf-d8af-4399-b6b1-c6599f507146",
            "TC-86aee159-98da-4d7e-aadf-8af9d91eedf2",
            "TC-d30d9cb8-0de3-4de2-9b32-2cffce6c4ab1",
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
