@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.routine.ui.detail

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.testing.routine.routine
import io.github.taetae98coding.diary.core.testing.routine.routineAmount
import io.github.taetae98coding.diary.core.testing.routine.routineDetail
import io.github.taetae98coding.diary.domain.routine.RoutineInput
import io.github.taetae98coding.diary.domain.routine.exception.RoutineAmountPerRecordInvalidException
import io.github.taetae98coding.diary.domain.routine.exception.RoutineGoalAmountInvalidException
import io.github.taetae98coding.diary.domain.routine.usecase.AddRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FindRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageRoutineRecordUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.UpdateRoutineUseCase
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordViewModel
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

class RoutineDetailViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-b6fd6edb-d8e4-4425-8d5e-73bffaed9f42 조회에 성공하면 저장된 정보로 내용 표시 상태가 된다") {
            runTest(mainDispatcher) {
                val routine = Arb.routine(isDeleted = false).next()
                val viewModel = viewModel(routine = flowOf(Result.success(routine)), id = routine.id)

                viewModel.uiState.test {
                    awaitItem() shouldBe RoutineDetailUiState.Loading
                    awaitItem() shouldBe RoutineDetailUiState.Content(id = routine.id, detail = routine.detail, isFinished = routine.isFinished)
                }
            }
        }

        test("TC-32b3f574-fee2-44c6-8c5b-a7c69d761731 대상 루틴이 없거나 조회에 실패하면 조회 중 상태를 유지한다") {
            listOf<Result<Routine?>>(Result.success(null), Result.failure(IllegalStateException(Arb.string().next()))).forEach { result ->
                runTest(mainDispatcher) {
                    val viewModel = viewModel(routine = flowOf(result))

                    viewModel.uiState.test {
                        awaitItem() shouldBe RoutineDetailUiState.Loading
                        advanceUntilIdle()
                        expectNoEvents()
                    }
                }
            }
        }

        test("TC-9b3c9a27-1920-4835-a64c-7580d8f0e91f 조회가 끝나지 않으면 조회 중 상태다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(routine = emptyFlow())

                viewModel.uiState.test {
                    awaitItem() shouldBe RoutineDetailUiState.Loading
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-556ffd48-fe0b-4afa-a1ed-8fd56c27b0be 저장된 내용이 다른 경로에서 바뀌면 바뀐 내용으로 갱신한다") {
            runTest(mainDispatcher) {
                val routine = Arb.routine(isDeleted = false).next()
                val changed = routine.copy(detail = Arb.routineDetail().next())
                val routineFlow = MutableStateFlow(Result.success<Routine?>(routine))
                val viewModel = viewModel(routine = routineFlow, id = routine.id)

                viewModel.uiState.test {
                    awaitItem() shouldBe RoutineDetailUiState.Loading
                    awaitItem() shouldBe RoutineDetailUiState.Content(id = routine.id, detail = routine.detail, isFinished = routine.isFinished)

                    routineFlow.value = Result.success(changed)

                    awaitItem() shouldBe RoutineDetailUiState.Content(id = routine.id, detail = changed.detail, isFinished = changed.isFinished)
                }
            }
        }

        test("TC-7aa9b736-ef04-4bd0-932d-c152ce7a3c26 대상 루틴이 삭제 상태가 되어도 내용 표시 상태를 유지한다") {
            runTest(mainDispatcher) {
                val routine = Arb.routine(isDeleted = false).next()
                val routineFlow = MutableStateFlow(Result.success<Routine?>(routine))
                val viewModel = viewModel(routine = routineFlow, id = routine.id)

                viewModel.uiState.test {
                    awaitItem() shouldBe RoutineDetailUiState.Loading
                    awaitItem() shouldBe RoutineDetailUiState.Content(id = routine.id, detail = routine.detail, isFinished = routine.isFinished)

                    routineFlow.value = Result.success(routine.copy(isDeleted = true))
                    advanceUntilIdle()

                    expectNoEvents()
                    viewModel.uiState.value shouldBe RoutineDetailUiState.Content(id = routine.id, detail = routine.detail, isFinished = routine.isFinished)
                }
            }
        }

        test("TC-00b425f1-19bc-46da-ae94-8d1dff94c926 수정에 성공하면 입력으로 수정을 한 번 요청하고 성공을 한 번 알린다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val input = routineInput()
                val updateRoutineUseCase = mockk<UpdateRoutineUseCase>()
                coEvery { updateRoutineUseCase(parameter = UpdateRoutineUseCase.Parameter(id = id, input = input)) } returns Result.success(1)
                val viewModel = viewModel(id = id, updateRoutineUseCase = updateRoutineUseCase)

                viewModel.effect.test {
                    viewModel.update(input = input)

                    awaitItem() shouldBe RoutineDetailEffect.UpdateSucceeded
                    expectNoEvents()
                }
                coVerify(exactly = 1) { updateRoutineUseCase(parameter = UpdateRoutineUseCase.Parameter(id = id, input = input)) }
            }
        }

        test("TC-62bb7013-b5d5-4f19-8f1b-8fb7dcf039d9 목표량을 비운 입력은 목표량 없이 수정을 요청하고 성공을 알린다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val input = routineInput().copy(goalAmount = null)
                val updateRoutineUseCase = mockk<UpdateRoutineUseCase>()
                coEvery { updateRoutineUseCase(parameter = UpdateRoutineUseCase.Parameter(id = id, input = input)) } returns Result.success(1)
                val viewModel = viewModel(id = id, updateRoutineUseCase = updateRoutineUseCase)

                viewModel.effect.test {
                    viewModel.update(input = input)

                    awaitItem() shouldBe RoutineDetailEffect.UpdateSucceeded
                    expectNoEvents()
                }
                coVerify(exactly = 1) { updateRoutineUseCase(parameter = UpdateRoutineUseCase.Parameter(id = id, input = input)) }
            }
        }

        test("TC-e3c3280d-7eb8-4450-929e-739edfa80a1a 수정을 처리하는 동안 진행 상태를 노출하고 끝나면 해제한다") {
            runTest(mainDispatcher) {
                val routine = Arb.routine(isDeleted = false).next()
                val gate = CompletableDeferred<Unit>()
                val updateRoutineUseCase = mockk<UpdateRoutineUseCase>()
                coEvery { updateRoutineUseCase(parameter = any()) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel = viewModel(routine = flowOf(Result.success(routine)), id = routine.id, updateRoutineUseCase = updateRoutineUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe RoutineDetailUiState.Loading
                    awaitItem() shouldBe RoutineDetailUiState.Content(id = routine.id, detail = routine.detail, isFinished = routine.isFinished)

                    viewModel.update(input = routineInput())
                    awaitItem() shouldBe RoutineDetailUiState.Content(id = routine.id, detail = routine.detail, isFinished = routine.isFinished, isUpdateInProgress = true)

                    gate.complete(Unit)
                    awaitItem() shouldBe RoutineDetailUiState.Content(id = routine.id, detail = routine.detail, isFinished = routine.isFinished)
                }
            }
        }

        test("TC-96e859a5-ce40-4b61-9c1d-3b568f637051 수정에 실패하면 안내 없이 진행 상태만 해제하고 다시 수정할 수 있다") {
            runTest(mainDispatcher) {
                val input = routineInput()
                val updateRoutineUseCase = mockk<UpdateRoutineUseCase>()
                coEvery { updateRoutineUseCase(parameter = any()) } returns Result.failure(IllegalStateException(Arb.string().next()))
                val viewModel = viewModel(updateRoutineUseCase = updateRoutineUseCase)

                viewModel.effect.test {
                    viewModel.update(input = input)
                    advanceUntilIdle()
                    viewModel.update(input = input)
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 2) { updateRoutineUseCase(parameter = any()) }
            }
        }

        test("TC-1f80875b-6ba8-4b49-a740-e89ff4114c08 목표량이 0이라 수정하지 않으면 목표량 입력이 필요함을 한 번 알린다") {
            runTest(mainDispatcher) {
                val updateRoutineUseCase = mockk<UpdateRoutineUseCase>()
                coEvery { updateRoutineUseCase(parameter = any()) } returns Result.failure(RoutineGoalAmountInvalidException())
                val viewModel = viewModel(updateRoutineUseCase = updateRoutineUseCase)

                viewModel.effect.test {
                    viewModel.update(input = routineInput().copy(goalAmount = 0))

                    awaitItem() shouldBe RoutineDetailEffect.GoalAmountInvalid
                    expectNoEvents()
                }
            }
        }

        test("TC-9f029fe0-6e88-4954-890e-dcacc8f860f6 한 번에 기록할 양이 0이라 수정하지 않으면 그 입력이 필요함을 한 번 알린다") {
            runTest(mainDispatcher) {
                val updateRoutineUseCase = mockk<UpdateRoutineUseCase>()
                coEvery { updateRoutineUseCase(parameter = any()) } returns Result.failure(RoutineAmountPerRecordInvalidException())
                val viewModel = viewModel(updateRoutineUseCase = updateRoutineUseCase)

                viewModel.effect.test {
                    viewModel.update(input = routineInput().copy(amountPerRecord = 0))

                    awaitItem() shouldBe RoutineDetailEffect.AmountPerRecordInvalid
                    expectNoEvents()
                }
            }
        }

        test("TC-adadec35-debe-400d-bc82-7895002dc187 삭제에 성공하면 삭제를 한 번 요청하고 화면을 떠나도록 한 번 알린다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val deleteRoutineUseCase = mockk<DeleteRoutineUseCase>()
                coEvery { deleteRoutineUseCase(parameter = id) } returns Result.success(1)
                val viewModel = viewModel(id = id, deleteRoutineUseCase = deleteRoutineUseCase)

                viewModel.effect.test {
                    viewModel.delete()

                    awaitItem() shouldBe RoutineDetailEffect.DeleteSucceeded
                    expectNoEvents()
                }
                coVerify(exactly = 1) { deleteRoutineUseCase(parameter = id) }
            }
        }

        test("TC-fee4712b-18ab-4c63-b78e-02c062cacce9 삭제를 처리하는 동안 진행 상태를 노출하고 수정은 요청한다") {
            runTest(mainDispatcher) {
                val routine = Arb.routine(isDeleted = false).next()
                val gate = CompletableDeferred<Unit>()
                val deleteRoutineUseCase = mockk<DeleteRoutineUseCase>()
                coEvery { deleteRoutineUseCase(parameter = routine.id) } coAnswers {
                    gate.await()
                    Result.failure(IllegalStateException(Arb.string().next()))
                }
                val updateRoutineUseCase = mockk<UpdateRoutineUseCase>()
                coEvery { updateRoutineUseCase(parameter = any()) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        routine = flowOf(Result.success(routine)),
                        id = routine.id,
                        updateRoutineUseCase = updateRoutineUseCase,
                        deleteRoutineUseCase = deleteRoutineUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe RoutineDetailUiState.Loading
                    awaitItem() shouldBe RoutineDetailUiState.Content(id = routine.id, detail = routine.detail, isFinished = routine.isFinished)

                    viewModel.delete()
                    awaitItem() shouldBe RoutineDetailUiState.Content(id = routine.id, detail = routine.detail, isFinished = routine.isFinished, isDeleteInProgress = true)

                    viewModel.update(input = routineInput())
                    runCurrent()
                    coVerify(exactly = 1) { updateRoutineUseCase(parameter = any()) }

                    gate.complete(Unit)
                    advanceUntilIdle()
                    viewModel.uiState.value shouldBe RoutineDetailUiState.Content(id = routine.id, detail = routine.detail, isFinished = routine.isFinished)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-69bd05c3-0cda-4460-b3e0-bb68e7ce106c 삭제에 실패하면 화면에 머무르고 다시 삭제할 수 있다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val deleteRoutineUseCase = mockk<DeleteRoutineUseCase>()
                coEvery { deleteRoutineUseCase(parameter = id) } returns Result.failure(IllegalStateException(Arb.string().next()))
                val viewModel = viewModel(id = id, deleteRoutineUseCase = deleteRoutineUseCase)

                viewModel.effect.test {
                    viewModel.delete()
                    advanceUntilIdle()
                    viewModel.delete()
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 2) { deleteRoutineUseCase(parameter = id) }
            }
        }

        test("TC-1180627c-8a42-4ce3-8ba6-7cde5f3c8100 내용 수정을 처리하는 동안 수정은 다시 요청하지 않고 삭제와 기록 넣기는 요청한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val gate = CompletableDeferred<Unit>()
                val updateRoutineUseCase = mockk<UpdateRoutineUseCase>()
                coEvery { updateRoutineUseCase(parameter = any()) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val deleteRoutineUseCase = mockk<DeleteRoutineUseCase>()
                coEvery { deleteRoutineUseCase(parameter = id) } returns Result.failure(IllegalStateException(Arb.string().next()))
                val addRoutineRecordUseCase = addRoutineRecordUseCase()
                val viewModel = viewModel(id = id, updateRoutineUseCase = updateRoutineUseCase, deleteRoutineUseCase = deleteRoutineUseCase)
                val recordViewModel = recordViewModel(routineId = id, addRoutineRecordUseCase = addRoutineRecordUseCase)

                viewModel.update(input = routineInput())
                runCurrent()
                viewModel.update(input = routineInput())
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

        test("TC-1180627c-8a42-4ce3-8ba6-7cde5f3c8100 삭제를 처리하는 동안 삭제는 다시 요청하지 않고 수정과 기록 넣기는 요청한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val gate = CompletableDeferred<Unit>()
                val deleteRoutineUseCase = mockk<DeleteRoutineUseCase>()
                coEvery { deleteRoutineUseCase(parameter = id) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val updateRoutineUseCase = mockk<UpdateRoutineUseCase>()
                coEvery { updateRoutineUseCase(parameter = any()) } returns Result.success(1)
                val addRoutineRecordUseCase = addRoutineRecordUseCase()
                val viewModel = viewModel(id = id, updateRoutineUseCase = updateRoutineUseCase, deleteRoutineUseCase = deleteRoutineUseCase)
                val recordViewModel = recordViewModel(routineId = id, addRoutineRecordUseCase = addRoutineRecordUseCase)

                viewModel.delete()
                runCurrent()
                viewModel.delete()
                viewModel.update(input = routineInput())
                recordViewModel.add(date = Arb.kotlinxLocalDate().next(), amount = Arb.routineAmount().next())
                runCurrent()

                coVerify(exactly = 1) { deleteRoutineUseCase(parameter = id) }
                coVerify(exactly = 1) { updateRoutineUseCase(parameter = any()) }
                coVerify(exactly = 1) { addRoutineRecordUseCase(parameter = any()) }
                gate.complete(Unit)
                advanceUntilIdle()
            }
        }

        test("TC-1180627c-8a42-4ce3-8ba6-7cde5f3c8100 기록 넣기를 처리하는 동안 기록 넣기는 다시 요청하지 않고 수정과 삭제는 요청한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val gate = CompletableDeferred<Unit>()
                val addRoutineRecordUseCase = mockk<AddRoutineRecordUseCase>()
                coEvery { addRoutineRecordUseCase(parameter = any()) } coAnswers {
                    gate.await()
                    Result.success(Arb.kotlinUuid().next())
                }
                val updateRoutineUseCase = mockk<UpdateRoutineUseCase>()
                coEvery { updateRoutineUseCase(parameter = any()) } returns Result.success(1)
                val deleteRoutineUseCase = mockk<DeleteRoutineUseCase>()
                coEvery { deleteRoutineUseCase(parameter = id) } returns Result.failure(IllegalStateException(Arb.string().next()))
                val viewModel = viewModel(id = id, updateRoutineUseCase = updateRoutineUseCase, deleteRoutineUseCase = deleteRoutineUseCase)
                val recordViewModel = recordViewModel(routineId = id, addRoutineRecordUseCase = addRoutineRecordUseCase)

                recordViewModel.add(date = Arb.kotlinxLocalDate().next(), amount = Arb.routineAmount().next())
                runCurrent()
                recordViewModel.add(date = Arb.kotlinxLocalDate().next(), amount = Arb.routineAmount().next())
                viewModel.update(input = routineInput())
                viewModel.delete()
                runCurrent()

                coVerify(exactly = 1) { addRoutineRecordUseCase(parameter = any()) }
                coVerify(exactly = 1) { updateRoutineUseCase(parameter = any()) }
                coVerify(exactly = 1) { deleteRoutineUseCase(parameter = id) }
                gate.complete(Unit)
                advanceUntilIdle()
            }
        }

        test("TC-1180627c-8a42-4ce3-8ba6-7cde5f3c8100 기록 A 빼기를 처리하는 동안 기록 A 빼기는 다시 요청하지 않고 기록 B 빼기는 요청한다") {
            runTest(mainDispatcher) {
                val recordAId = Arb.kotlinUuid().next()
                val recordBId = Arb.kotlinUuid().next()
                val gate = CompletableDeferred<Unit>()
                val deleteRoutineRecordUseCase = mockk<DeleteRoutineRecordUseCase>()
                coEvery { deleteRoutineRecordUseCase(parameter = any()) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val recordViewModel = recordViewModel(routineId = Arb.kotlinUuid().next(), deleteRoutineRecordUseCase = deleteRoutineRecordUseCase)

                recordViewModel.delete(recordId = recordAId)
                runCurrent()
                recordViewModel.delete(recordId = recordAId)
                recordViewModel.delete(recordId = recordBId)
                runCurrent()

                coVerify(exactly = 1) { deleteRoutineRecordUseCase(parameter = recordAId) }
                coVerify(exactly = 1) { deleteRoutineRecordUseCase(parameter = recordBId) }
                gate.complete(Unit)
                advanceUntilIdle()
            }
        }
    }

    private companion object {
        fun routineInput(): RoutineInput {
            val detail = Arb.routineDetail().next()

            return RoutineInput(
                title = detail.title,
                description = detail.description,
                color = detail.color,
                startDate = detail.startDate,
                cycle = detail.cycle,
                unit = detail.unit,
                goalAmount = detail.goalAmount,
                amountPerRecord = detail.amountPerRecord,
            )
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
            deleteRoutineUseCase: DeleteRoutineUseCase = mockk(),
        ): RoutineDetailViewModel {
            val findRoutineUseCase = mockk<FindRoutineUseCase>()
            every { findRoutineUseCase(parameter = id) } returns routine

            return RoutineDetailViewModel(
                id = id,
                updateRoutineUseCase = updateRoutineUseCase,
                finishRoutineUseCase = mockk(),
                restartRoutineUseCase = mockk(),
                deleteRoutineUseCase = deleteRoutineUseCase,
                findRoutineUseCase = findRoutineUseCase,
            )
        }

        fun recordViewModel(
            routineId: Uuid,
            addRoutineRecordUseCase: AddRoutineRecordUseCase = mockk(),
            deleteRoutineRecordUseCase: DeleteRoutineRecordUseCase = mockk(),
        ): RoutineDetailRecordViewModel {
            val pageRoutineRecordUseCase = mockk<PageRoutineRecordUseCase>()
            every { pageRoutineRecordUseCase(parameter = routineId) } returns emptyFlow()

            return RoutineDetailRecordViewModel(
                routineId = routineId,
                addRoutineRecordUseCase = addRoutineRecordUseCase,
                deleteRoutineRecordUseCase = deleteRoutineRecordUseCase,
                pageRoutineRecordUseCase = pageRoutineRecordUseCase,
            )
        }
    }
}
