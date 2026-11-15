@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.routine.ui.finished

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import io.github.taetae98coding.diary.compose.routine.RoutineListEffect
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.testing.routine.routineSummary
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FinishRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageFinishedRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestartRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestoreRoutineUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus

class RoutineFinishedListViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("오늘이 정해지면 그 오늘로 조회한 완료된 루틴 페이지를 그대로 노출한다") {
            runTest(mainDispatcher) {
                val today = Arb.kotlinxLocalDate().next()
                val summaryList = List(3) { Arb.routineSummary().next() }
                val viewModel = viewModel(pageFlow = { flowOf(Result.success(PagingData.from(summaryList))) })

                viewModel.updateToday(today = today)

                flowOf(viewModel.routinePagingData.first()).asSnapshot() shouldBe summaryList
            }
        }

        test("TC-e1a92649-5a7e-4ca4-856f-c0ed093bd43d 오늘이 다시 정해지면 새 오늘로 완료된 루틴을 다시 조회한다") {
            runTest(mainDispatcher) {
                val today = Arb.kotlinxLocalDate().next()
                val nextDay = today.plus(1, DateTimeUnit.DAY)
                val nextDaySummaryList = List(2) { Arb.routineSummary().next() }
                val pageFinishedRoutineUseCase = mockk<PageFinishedRoutineUseCase>()
                every { pageFinishedRoutineUseCase(parameter = today) } returns flowOf(Result.success(PagingData.from(emptyList())))
                every { pageFinishedRoutineUseCase(parameter = nextDay) } returns flowOf(Result.success(PagingData.from(nextDaySummaryList)))
                val viewModel = viewModel(pageFinishedRoutineUseCase = pageFinishedRoutineUseCase)

                viewModel.routinePagingData.test {
                    viewModel.updateToday(today = today)
                    advanceUntilIdle()
                    viewModel.updateToday(today = nextDay)
                    advanceUntilIdle()

                    flowOf(expectMostRecentItem()).asSnapshot() shouldBe nextDaySummaryList
                }
                verify(exactly = 1) { pageFinishedRoutineUseCase(parameter = nextDay) }
            }
        }

        test("TC-0f0c4820-3db5-456d-9932-6f8945c81b79 처음 불러오기에 실패하면 노출하는 루틴이 없다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(pageFlow = { flowOf(Result.failure(IllegalStateException("저장하거나 불러오지 못함"))) })

                viewModel.updateToday(today = Arb.kotlinxLocalDate().next())

                flowOf(viewModel.routinePagingData.first()).asSnapshot().shouldBeEmpty()
            }
        }

        test("TC-0f0c4820-3db5-456d-9932-6f8945c81b79 루틴이 보이는 목록을 다시 불러오다 실패하면 앞서 보이던 루틴을 남기지 않는다") {
            runTest(mainDispatcher) {
                val pageFlow = MutableStateFlow<Result<PagingData<RoutineSummary>>>(Result.success(PagingData.from(List(2) { Arb.routineSummary().next() })))
                val viewModel = viewModel(pageFlow = { pageFlow })

                viewModel.routinePagingData.test {
                    viewModel.updateToday(today = Arb.kotlinxLocalDate().next())
                    advanceUntilIdle()
                    pageFlow.value = Result.failure(IllegalStateException("저장하거나 불러오지 못함"))
                    advanceUntilIdle()

                    flowOf(expectMostRecentItem()).asSnapshot().shouldBeEmpty()
                }
            }
        }

        test("TC-8a130cbb-7635-4bfc-830f-8f9d116e441f 다시 시작하면 그 루틴의 다시 시작을 한 번 요청하고 다시 시작 Effect를 보낸다") {
            runTest(mainDispatcher) {
                val routineId = Arb.kotlinUuid().next()
                val restartRoutineUseCase = mockk<RestartRoutineUseCase>()
                coEvery { restartRoutineUseCase(parameter = any()) } returns Result.success(1)
                val viewModel = viewModel(restartRoutineUseCase = restartRoutineUseCase)

                viewModel.effect.test {
                    viewModel.restart(id = routineId)
                    advanceUntilIdle()

                    awaitItem() shouldBe RoutineListEffect.Restarted(id = routineId)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { restartRoutineUseCase(parameter = routineId) }
            }
        }

        test("TC-ea599f9a-8586-4971-bf46-80f51a8676a8 삭제하면 그 루틴의 삭제를 한 번 요청하고 삭제 Effect를 보낸다") {
            runTest(mainDispatcher) {
                val routineId = Arb.kotlinUuid().next()
                val deleteRoutineUseCase = mockk<DeleteRoutineUseCase>()
                coEvery { deleteRoutineUseCase(parameter = any()) } returns Result.success(1)
                val viewModel = viewModel(deleteRoutineUseCase = deleteRoutineUseCase)

                viewModel.effect.test {
                    viewModel.delete(id = routineId)
                    advanceUntilIdle()

                    awaitItem() shouldBe RoutineListEffect.Deleted(id = routineId)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { deleteRoutineUseCase(parameter = routineId) }
            }
        }

        test("TC-22a7c048-68cf-4e88-affc-8e6700cd2b36 다시 시작을 실행 취소하면 그 루틴의 완료를 한 번 요청하고 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val routineId = Arb.kotlinUuid().next()
                val finishRoutineUseCase = mockk<FinishRoutineUseCase>()
                coEvery { finishRoutineUseCase(parameter = any()) } returns Result.success(1)
                val viewModel = viewModel(finishRoutineUseCase = finishRoutineUseCase)

                viewModel.effect.test {
                    viewModel.undo(effect = RoutineListEffect.Restarted(id = routineId))
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { finishRoutineUseCase(parameter = routineId) }
            }
        }

        test("TC-e59bd1c7-9f62-4fcf-bdb0-be5a48d29e53 삭제를 실행 취소하면 그 루틴의 복구를 한 번 요청하고 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val routineId = Arb.kotlinUuid().next()
                val restoreRoutineUseCase = mockk<RestoreRoutineUseCase>()
                coEvery { restoreRoutineUseCase(parameter = any()) } returns Result.success(1)
                val viewModel = viewModel(restoreRoutineUseCase = restoreRoutineUseCase)

                viewModel.effect.test {
                    viewModel.undo(effect = RoutineListEffect.Deleted(id = routineId))
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { restoreRoutineUseCase(parameter = routineId) }
            }
        }

        test("TC-5699aba0-889f-4377-a9e0-cc3cdc7be03b 다시 시작이나 삭제가 저장되지 못하면 Effect를 보내지 않는다") {
            runTest(mainDispatcher) {
                val routineId = Arb.kotlinUuid().next()
                val restartRoutineUseCase = mockk<RestartRoutineUseCase>()
                val deleteRoutineUseCase = mockk<DeleteRoutineUseCase>()
                coEvery { restartRoutineUseCase(parameter = any()) } returns Result.failure(IllegalStateException("저장하거나 불러오지 못함"))
                coEvery { deleteRoutineUseCase(parameter = any()) } returns Result.failure(IllegalStateException("저장하거나 불러오지 못함"))
                val viewModel = viewModel(restartRoutineUseCase = restartRoutineUseCase, deleteRoutineUseCase = deleteRoutineUseCase)

                viewModel.effect.test {
                    viewModel.restart(id = routineId)
                    viewModel.delete(id = routineId)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }
    }

    private fun viewModel(
        pageFlow: () -> Flow<Result<PagingData<RoutineSummary>>> = { flowOf(Result.success(PagingData.empty())) },
        pageFinishedRoutineUseCase: PageFinishedRoutineUseCase = mockk<PageFinishedRoutineUseCase>().also { useCase -> every { useCase(parameter = any()) } answers { pageFlow() } },
        finishRoutineUseCase: FinishRoutineUseCase = mockk(),
        restartRoutineUseCase: RestartRoutineUseCase = mockk(),
        deleteRoutineUseCase: DeleteRoutineUseCase = mockk(),
        restoreRoutineUseCase: RestoreRoutineUseCase = mockk(),
    ): RoutineFinishedListViewModel =
        RoutineFinishedListViewModel(
            pageFinishedRoutineUseCase = pageFinishedRoutineUseCase,
            finishRoutineUseCase = finishRoutineUseCase,
            restartRoutineUseCase = restartRoutineUseCase,
            deleteRoutineUseCase = deleteRoutineUseCase,
            restoreRoutineUseCase = restoreRoutineUseCase,
        )
}
