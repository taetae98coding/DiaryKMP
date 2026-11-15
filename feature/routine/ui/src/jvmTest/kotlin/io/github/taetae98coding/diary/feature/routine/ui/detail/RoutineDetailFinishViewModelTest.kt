@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.routine.ui.detail

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.testing.routine.routine
import io.github.taetae98coding.diary.core.testing.routine.routineAmount
import io.github.taetae98coding.diary.domain.routine.usecase.AddRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FindRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FinishRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestartRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.UpdateRoutineUseCase
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordViewModel
import io.github.taetae98coding.diary.feature.routine.ui.form.routineInput
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

class RoutineDetailFinishViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-294fd7f4-c40e-4b3f-92d5-835d240f2ffc 내용 표시 상태는 저장된 완료 여부를 담는다") {
            listOf(false, true).forEach { isFinished ->
                runTest(mainDispatcher) {
                    val routine = Arb.routine(isDeleted = false, isFinished = isFinished).next()
                    val viewModel = viewModel(routine = flowOf(Result.success(routine)), id = routine.id)

                    viewModel.uiState.test {
                        awaitItem() shouldBe RoutineDetailUiState.Loading
                        awaitItem() shouldBe RoutineDetailUiState.Content(id = routine.id, detail = routine.detail, isFinished = isFinished)
                    }
                }
            }
        }

        test("TC-84ca3874-025d-41cf-85a7-18d7564695ff 완료를 선택하면 완료를 한 번 요청하고 알리는 것이 없다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val finishRoutineUseCase = mockk<FinishRoutineUseCase>()
                coEvery { finishRoutineUseCase(parameter = id) } returns Result.success(1)
                val viewModel = viewModel(id = id, finishRoutineUseCase = finishRoutineUseCase)

                viewModel.effect.test {
                    viewModel.finish()
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { finishRoutineUseCase(parameter = id) }
            }
        }

        test("TC-10797a39-0249-4871-98fe-43b7ecd75bb8 다시 시작을 선택하면 다시 시작을 한 번 요청하고 알리는 것이 없다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val restartRoutineUseCase = mockk<RestartRoutineUseCase>()
                coEvery { restartRoutineUseCase(parameter = id) } returns Result.success(1)
                val viewModel = viewModel(id = id, restartRoutineUseCase = restartRoutineUseCase)

                viewModel.effect.test {
                    viewModel.restart()
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { restartRoutineUseCase(parameter = id) }
            }
        }

        test("TC-6911145f-75ae-4699-9127-8fb4783224b2 완료나 다시 시작을 처리하는 동안 진행 상태를 노출하고 끝나면 해제한다") {
            listOf(false, true).forEach { isFinished ->
                runTest(mainDispatcher) {
                    val routine = Arb.routine(isDeleted = false, isFinished = isFinished).next()
                    val gate = CompletableDeferred<Unit>()
                    val finishRoutineUseCase = mockk<FinishRoutineUseCase>()
                    val restartRoutineUseCase = mockk<RestartRoutineUseCase>()
                    coEvery { finishRoutineUseCase(parameter = routine.id) } coAnswers {
                        gate.await()
                        Result.success(1)
                    }
                    coEvery { restartRoutineUseCase(parameter = routine.id) } coAnswers {
                        gate.await()
                        Result.success(1)
                    }
                    val viewModel =
                        viewModel(
                            routine = flowOf(Result.success(routine)),
                            id = routine.id,
                            finishRoutineUseCase = finishRoutineUseCase,
                            restartRoutineUseCase = restartRoutineUseCase,
                        )
                    val content = RoutineDetailUiState.Content(id = routine.id, detail = routine.detail, isFinished = isFinished)

                    viewModel.uiState.test {
                        awaitItem() shouldBe RoutineDetailUiState.Loading
                        awaitItem() shouldBe content

                        if (isFinished) viewModel.restart() else viewModel.finish()
                        awaitItem() shouldBe content.copy(isFinishInProgress = true)

                        gate.complete(Unit)
                        awaitItem() shouldBe content
                    }
                }
            }
        }

        test("TC-7d6fdf91-b086-45d5-bba7-92302d3550a2 완료나 다시 시작에 실패하면 알리지 않고 진행 상태만 해제해 다시 실행할 수 있다") {
            listOf(false, true).forEach { isFinished ->
                runTest(mainDispatcher) {
                    val routine = Arb.routine(isDeleted = false, isFinished = isFinished).next()
                    val finishRoutineUseCase = mockk<FinishRoutineUseCase>()
                    val restartRoutineUseCase = mockk<RestartRoutineUseCase>()
                    coEvery { finishRoutineUseCase(parameter = routine.id) } returns Result.failure(IllegalStateException(Arb.string().next()))
                    coEvery { restartRoutineUseCase(parameter = routine.id) } returns Result.failure(IllegalStateException(Arb.string().next()))
                    val viewModel =
                        viewModel(
                            routine = flowOf(Result.success(routine)),
                            id = routine.id,
                            finishRoutineUseCase = finishRoutineUseCase,
                            restartRoutineUseCase = restartRoutineUseCase,
                        )
                    val action = { if (isFinished) viewModel.restart() else viewModel.finish() }

                    viewModel.effect.test {
                        action()
                        advanceUntilIdle()
                        action()
                        advanceUntilIdle()

                        expectNoEvents()
                    }
                    viewModel.uiState.test {
                        skipItems(1)
                        awaitItem() shouldBe RoutineDetailUiState.Content(id = routine.id, detail = routine.detail, isFinished = isFinished)
                    }
                    coVerify(exactly = if (isFinished) 0 else 2) { finishRoutineUseCase(parameter = routine.id) }
                    coVerify(exactly = if (isFinished) 2 else 0) { restartRoutineUseCase(parameter = routine.id) }
                }
            }
        }

        test("TC-f64538e2-c9cf-4809-827e-7e83242d23e9 저장된 완료 여부가 다른 경로에서 바뀌면 바뀐 완료 여부로 갱신한다") {
            listOf(false, true).forEach { isFinished ->
                runTest(mainDispatcher) {
                    val routine = Arb.routine(isDeleted = false, isFinished = isFinished).next()
                    val routineFlow = MutableStateFlow(Result.success<Routine?>(routine))
                    val viewModel = viewModel(routine = routineFlow, id = routine.id)

                    viewModel.uiState.test {
                        awaitItem() shouldBe RoutineDetailUiState.Loading
                        awaitItem() shouldBe RoutineDetailUiState.Content(id = routine.id, detail = routine.detail, isFinished = isFinished)

                        routineFlow.value = Result.success(routine.copy(isFinished = !isFinished))

                        awaitItem() shouldBe RoutineDetailUiState.Content(id = routine.id, detail = routine.detail, isFinished = !isFinished)
                    }
                }
            }
        }

        test("TC-d9be343f-9cfe-41e5-a2c7-bd55fc699f95 완료나 다시 시작을 처리하는 동안 완료와 다시 시작은 다시 요청하지 않는다") {
            listOf(false, true).forEach { isRestartFirst ->
                runTest(mainDispatcher) {
                    val id = Arb.kotlinUuid().next()
                    val gate = CompletableDeferred<Unit>()
                    val finishRoutineUseCase = mockk<FinishRoutineUseCase>()
                    val restartRoutineUseCase = mockk<RestartRoutineUseCase>()
                    coEvery { finishRoutineUseCase(parameter = id) } coAnswers {
                        gate.await()
                        Result.success(1)
                    }
                    coEvery { restartRoutineUseCase(parameter = id) } coAnswers {
                        gate.await()
                        Result.success(1)
                    }
                    val viewModel = viewModel(id = id, finishRoutineUseCase = finishRoutineUseCase, restartRoutineUseCase = restartRoutineUseCase)

                    if (isRestartFirst) viewModel.restart() else viewModel.finish()
                    runCurrent()
                    viewModel.finish()
                    viewModel.restart()
                    runCurrent()

                    coVerify(exactly = if (isRestartFirst) 0 else 1) { finishRoutineUseCase(parameter = id) }
                    coVerify(exactly = if (isRestartFirst) 1 else 0) { restartRoutineUseCase(parameter = id) }
                    gate.complete(Unit)
                    advanceUntilIdle()
                }
            }
        }

        test("TC-d9be343f-9cfe-41e5-a2c7-bd55fc699f95 완료를 처리하는 동안 수정, 삭제와 기록 넣기는 요청한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val gate = CompletableDeferred<Unit>()
                val finishRoutineUseCase = mockk<FinishRoutineUseCase>()
                coEvery { finishRoutineUseCase(parameter = id) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val updateRoutineUseCase = failingUpdateRoutineUseCase()
                val deleteRoutineUseCase = failingDeleteRoutineUseCase(id = id)
                val addRoutineRecordUseCase = addRoutineRecordUseCase()
                val viewModel =
                    viewModel(
                        id = id,
                        updateRoutineUseCase = updateRoutineUseCase,
                        finishRoutineUseCase = finishRoutineUseCase,
                        deleteRoutineUseCase = deleteRoutineUseCase,
                    )
                val recordViewModel = recordViewModel(routineId = id, addRoutineRecordUseCase = addRoutineRecordUseCase)

                viewModel.finish()
                runCurrent()
                viewModel.update(input = Arb.routineInput().next())
                viewModel.delete()
                recordViewModel.add(date = Arb.kotlinxLocalDate().next(), amount = Arb.routineAmount().next())
                runCurrent()

                coVerify(exactly = 1) { updateRoutineUseCase(parameter = any()) }
                coVerify(exactly = 1) { deleteRoutineUseCase(parameter = id) }
                coVerify(exactly = 1) { addRoutineRecordUseCase(parameter = any()) }
                gate.complete(Unit)
                advanceUntilIdle()
            }
        }

        test("TC-d9be343f-9cfe-41e5-a2c7-bd55fc699f95 수정, 삭제나 기록 넣기를 처리하는 동안 완료는 요청한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val gate = CompletableDeferred<Unit>()
                val updateRoutineUseCase = mockk<UpdateRoutineUseCase>()
                coEvery { updateRoutineUseCase(parameter = any()) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val deleteRoutineUseCase = mockk<DeleteRoutineUseCase>()
                coEvery { deleteRoutineUseCase(parameter = id) } coAnswers {
                    gate.await()
                    Result.failure(IllegalStateException(Arb.string().next()))
                }
                val addRoutineRecordUseCase = mockk<AddRoutineRecordUseCase>()
                coEvery { addRoutineRecordUseCase(parameter = any()) } coAnswers {
                    gate.await()
                    Result.success(Arb.kotlinUuid().next())
                }
                val finishRoutineUseCase = mockk<FinishRoutineUseCase>()
                coEvery { finishRoutineUseCase(parameter = id) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        id = id,
                        updateRoutineUseCase = updateRoutineUseCase,
                        finishRoutineUseCase = finishRoutineUseCase,
                        deleteRoutineUseCase = deleteRoutineUseCase,
                    )
                val recordViewModel = recordViewModel(routineId = id, addRoutineRecordUseCase = addRoutineRecordUseCase)

                viewModel.update(input = Arb.routineInput().next())
                viewModel.delete()
                recordViewModel.add(date = Arb.kotlinxLocalDate().next(), amount = Arb.routineAmount().next())
                runCurrent()
                viewModel.finish()
                runCurrent()

                coVerify(exactly = 1) { finishRoutineUseCase(parameter = id) }
                gate.complete(Unit)
                advanceUntilIdle()
            }
        }
    }

    private companion object {
        fun failingUpdateRoutineUseCase(): UpdateRoutineUseCase {
            val useCase = mockk<UpdateRoutineUseCase>()
            coEvery { useCase(parameter = any()) } returns Result.failure(IllegalStateException(Arb.string().next()))
            return useCase
        }

        fun failingDeleteRoutineUseCase(id: Uuid): DeleteRoutineUseCase {
            val useCase = mockk<DeleteRoutineUseCase>()
            coEvery { useCase(parameter = id) } returns Result.failure(IllegalStateException(Arb.string().next()))
            return useCase
        }

        fun addRoutineRecordUseCase(): AddRoutineRecordUseCase {
            val useCase = mockk<AddRoutineRecordUseCase>()
            coEvery { useCase(parameter = any()) } returns Result.success(Arb.kotlinUuid().next())
            return useCase
        }

        fun viewModel(
            routine: Flow<Result<Routine?>> = emptyFlow(),
            id: Uuid = Arb.kotlinUuid().next(),
            updateRoutineUseCase: UpdateRoutineUseCase = mockk(),
            finishRoutineUseCase: FinishRoutineUseCase = mockk(),
            restartRoutineUseCase: RestartRoutineUseCase = mockk(),
            deleteRoutineUseCase: DeleteRoutineUseCase = mockk(),
        ): RoutineDetailViewModel {
            val findRoutineUseCase = mockk<FindRoutineUseCase>()
            every { findRoutineUseCase(parameter = id) } returns routine

            return RoutineDetailViewModel(
                id = id,
                updateRoutineUseCase = updateRoutineUseCase,
                finishRoutineUseCase = finishRoutineUseCase,
                restartRoutineUseCase = restartRoutineUseCase,
                deleteRoutineUseCase = deleteRoutineUseCase,
                findRoutineUseCase = findRoutineUseCase,
            )
        }

        fun recordViewModel(
            routineId: Uuid,
            addRoutineRecordUseCase: AddRoutineRecordUseCase,
        ): RoutineDetailRecordViewModel {
            val pageRoutineRecordUseCase = mockk<PageRoutineRecordUseCase>()
            every { pageRoutineRecordUseCase(parameter = routineId) } returns emptyFlow()

            return RoutineDetailRecordViewModel(
                routineId = routineId,
                addRoutineRecordUseCase = addRoutineRecordUseCase,
                deleteRoutineRecordUseCase = mockk(),
                pageRoutineRecordUseCase = pageRoutineRecordUseCase,
            )
        }
    }
}
