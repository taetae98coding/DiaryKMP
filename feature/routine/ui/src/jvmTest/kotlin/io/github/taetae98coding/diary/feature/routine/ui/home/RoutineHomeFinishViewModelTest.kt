@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.paging.PagingData
import app.cash.turbine.test
import io.github.taetae98coding.diary.compose.routine.RoutineListEffect
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.routine.usecase.FinishRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.GetRoutineFilterTagIdUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.GetRoutineFilterUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageViewScopeRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestartRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestoreRoutineUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class RoutineHomeFinishViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-08080f94-9198-4ced-a92b-e291989f0576 목록에서 완료하면 그 루틴의 완료를 한 번 요청하고 완료 Effect를 보낸다") {
            runTest(mainDispatcher) {
                val routineId = Arb.kotlinUuid().next()
                val finishRoutineUseCase = mockk<FinishRoutineUseCase>()
                coEvery { finishRoutineUseCase(parameter = any()) } returns Result.success(1)
                val viewModel = viewModel(finishRoutineUseCase = finishRoutineUseCase)

                viewModel.effect.test {
                    viewModel.finish(id = routineId)
                    advanceUntilIdle()

                    awaitItem() shouldBe RoutineListEffect.Finished(id = routineId)
                    expectNoEvents()
                }

                coVerify(exactly = 1) { finishRoutineUseCase(parameter = routineId) }
            }
        }

        test("TC-be59232a-b39c-44d3-8d35-29bf42965bf7 완료가 저장되지 못하면 완료 Effect를 보내지 않고 다시 완료할 수 있다") {
            runTest(mainDispatcher) {
                val routineId = Arb.kotlinUuid().next()
                val finishRoutineUseCase = mockk<FinishRoutineUseCase>()
                coEvery { finishRoutineUseCase(parameter = any()) } returnsMany listOf(Result.failure(IllegalStateException(Arb.string().next())), Result.success(1))
                val viewModel = viewModel(finishRoutineUseCase = finishRoutineUseCase)

                viewModel.effect.test {
                    viewModel.finish(id = routineId)
                    advanceUntilIdle()
                    expectNoEvents()

                    viewModel.finish(id = routineId)
                    advanceUntilIdle()

                    awaitItem() shouldBe RoutineListEffect.Finished(id = routineId)
                    expectNoEvents()
                }

                coVerify(exactly = 2) { finishRoutineUseCase(parameter = routineId) }
            }
        }

        test("TC-1730d138-f1e8-4c16-a918-3d633792065e 완료를 실행 취소하면 그 루틴을 완료되지 않은 상태로 되돌리는 요청을 한 번 하고 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val routineId = Arb.kotlinUuid().next()
                val restartRoutineUseCase = mockk<RestartRoutineUseCase>()
                val restoreRoutineUseCase = mockk<RestoreRoutineUseCase>()
                coEvery { restartRoutineUseCase(parameter = any()) } returns Result.success(1)
                val viewModel = viewModel(restartRoutineUseCase = restartRoutineUseCase, restoreRoutineUseCase = restoreRoutineUseCase)

                viewModel.effect.test {
                    viewModel.undo(effect = RoutineListEffect.Finished(id = routineId))
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 1) { restartRoutineUseCase(parameter = routineId) }
                coVerify(exactly = 0) { restoreRoutineUseCase(parameter = any()) }
            }
        }

        test("TC-671812a3-cadc-4364-a28a-ee5458ab2643 완료의 실행 취소를 저장하지 못해도 별도 안내 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val routineId = Arb.kotlinUuid().next()
                val restartRoutineUseCase = mockk<RestartRoutineUseCase>()
                coEvery { restartRoutineUseCase(parameter = any()) } returns Result.failure(IllegalStateException(Arb.string().next()))
                val viewModel = viewModel(restartRoutineUseCase = restartRoutineUseCase)

                viewModel.effect.test {
                    viewModel.undo(effect = RoutineListEffect.Finished(id = routineId))
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 1) { restartRoutineUseCase(parameter = routineId) }
            }
        }

        test("같은 루틴의 완료를 처리하는 동안 다시 요청한 완료는 처리하지 않는다") {
            runTest(mainDispatcher) {
                val routineId = Arb.kotlinUuid().next()
                val gate = CompletableDeferred<Unit>()
                val finishRoutineUseCase = mockk<FinishRoutineUseCase>()
                coEvery { finishRoutineUseCase(parameter = any()) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel = viewModel(finishRoutineUseCase = finishRoutineUseCase)

                viewModel.effect.test {
                    viewModel.finish(id = routineId)
                    viewModel.finish(id = routineId)
                    advanceUntilIdle()
                    gate.complete(Unit)
                    advanceUntilIdle()

                    awaitItem() shouldBe RoutineListEffect.Finished(id = routineId)
                    expectNoEvents()
                }

                coVerify(exactly = 1) { finishRoutineUseCase(parameter = routineId) }
            }
        }
    }

    private fun viewModel(
        finishRoutineUseCase: FinishRoutineUseCase = mockk(),
        restartRoutineUseCase: RestartRoutineUseCase = mockk(),
        restoreRoutineUseCase: RestoreRoutineUseCase = mockk(),
    ): RoutineHomeViewModel {
        val pageRoutineUseCase = mockk<PageViewScopeRoutineUseCase>()
        every { pageRoutineUseCase(parameter = any()) } returns flowOf(Result.success(PagingData.empty()))
        val getRoutineFilterUseCase = mockk<GetRoutineFilterUseCase>()
        every { getRoutineFilterUseCase() } returns flowOf(Result.success(emptyList()))
        val getRoutineFilterTagIdUseCase = mockk<GetRoutineFilterTagIdUseCase>()
        every { getRoutineFilterTagIdUseCase() } returns flowOf(Result.success(emptySet()))

        return RoutineHomeViewModel(
            getRoutineFilterUseCase = getRoutineFilterUseCase,
            getRoutineFilterTagIdUseCase = getRoutineFilterTagIdUseCase,
            getRoutineViewScopeUseCase = routineViewScopeUseCase(),
            getUnfinishedRoutineExistsUseCase = unfinishedRoutineExistsUseCase(),
            pageViewScopeRoutineUseCase = pageRoutineUseCase,
            finishRoutineUseCase = finishRoutineUseCase,
            restartRoutineUseCase = restartRoutineUseCase,
            deleteRoutineUseCase = mockk(),
            restoreRoutineUseCase = restoreRoutineUseCase,
            addQuickRoutineRecordUseCase = mockk(),
            deleteRoutineRecordUseCase = mockk(),
        )
    }
}
