@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.dday.ui.detail

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.testing.dday.dday
import io.github.taetae98coding.diary.core.testing.dday.ddayDetail
import io.github.taetae98coding.diary.domain.dday.exception.DDayRepeatBlankException
import io.github.taetae98coding.diary.domain.dday.usecase.DeleteDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.FindDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.UpdateDDayUseCase
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayInput
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

class DDayDetailViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-2d477ce7-412d-4170-a861-e1750504a898 조회에 성공하면 저장된 정보로 내용 표시 상태가 된다") {
            runTest(mainDispatcher) {
                val dday = Arb.dday(isDeleted = false).next()
                val viewModel = viewModel(dday = flowOf(Result.success(dday)), id = dday.id)

                viewModel.uiState.test {
                    awaitItem() shouldBe DDayDetailUiState.Loading
                    awaitItem() shouldBe DDayDetailUiState.Content(id = dday.id, detail = dday.detail)
                }
            }
        }

        test("TC-0759207b-2d41-48bc-a6e0-009446d644b0 대상 디데이가 없거나 조회에 실패하면 조회 중 상태를 유지한다") {
            listOf<Result<DDay?>>(Result.success(null), Result.failure(IllegalStateException(Arb.string().next()))).forEach { result ->
                runTest(mainDispatcher) {
                    val viewModel = viewModel(dday = flowOf(result))

                    viewModel.uiState.test {
                        awaitItem() shouldBe DDayDetailUiState.Loading
                        advanceUntilIdle()
                        expectNoEvents()
                    }
                }
            }
        }

        test("TC-1a600949-d45a-44f9-b294-98e25e4d5d32 조회가 끝나지 않으면 조회 중 상태다") {
            runTest(mainDispatcher) {
                val viewModel = viewModel(dday = emptyFlow())

                viewModel.uiState.test {
                    awaitItem() shouldBe DDayDetailUiState.Loading
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("TC-ba4397d4-64ba-432d-8886-2ae6fa3fe0ff 저장된 내용이 다른 경로에서 바뀌면 바뀐 내용으로 갱신한다") {
            runTest(mainDispatcher) {
                val dday = Arb.dday(isDeleted = false).next()
                val changed = dday.copy(detail = Arb.ddayDetail().next())
                val ddayFlow = MutableStateFlow(Result.success<DDay?>(dday))
                val viewModel = viewModel(dday = ddayFlow, id = dday.id)

                viewModel.uiState.test {
                    awaitItem() shouldBe DDayDetailUiState.Loading
                    awaitItem() shouldBe DDayDetailUiState.Content(id = dday.id, detail = dday.detail)

                    ddayFlow.value = Result.success(changed)

                    awaitItem() shouldBe DDayDetailUiState.Content(id = dday.id, detail = changed.detail)
                }
            }
        }

        test("TC-0ea235fb-fad7-4e8a-ae24-dc4f41180732 대상 디데이가 삭제 상태가 되어도 내용 표시 상태를 유지한다") {
            runTest(mainDispatcher) {
                val dday = Arb.dday(isDeleted = false).next()
                val ddayFlow = MutableStateFlow(Result.success<DDay?>(dday))
                val viewModel = viewModel(dday = ddayFlow, id = dday.id)

                viewModel.uiState.test {
                    awaitItem() shouldBe DDayDetailUiState.Loading
                    awaitItem() shouldBe DDayDetailUiState.Content(id = dday.id, detail = dday.detail)

                    ddayFlow.value = Result.success(dday.copy(isDeleted = true))
                    advanceUntilIdle()

                    expectNoEvents()
                    viewModel.uiState.value shouldBe DDayDetailUiState.Content(id = dday.id, detail = dday.detail)
                }
            }
        }

        test("TC-05982e66-411b-43ea-8479-c994c91a3383 수정에 성공하면 입력으로 수정을 한 번 요청하고 성공을 한 번 알린다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val input = Arb.ddayInput().next()
                val updateDDayUseCase = mockk<UpdateDDayUseCase>()
                coEvery { updateDDayUseCase(parameter = UpdateDDayUseCase.Parameter(id = id, input = input)) } returns Result.success(1)
                val viewModel = viewModel(id = id, updateDDayUseCase = updateDDayUseCase)

                viewModel.effect.test {
                    viewModel.update(input = input)

                    awaitItem() shouldBe DDayDetailEffect.UpdateSucceeded
                    expectNoEvents()
                }
                coVerify(exactly = 1) { updateDDayUseCase(parameter = UpdateDDayUseCase.Parameter(id = id, input = input)) }
            }
        }

        test("TC-cbe821d7-7fa6-48cb-91b1-8847d83889e4 제목을 비운 입력도 그대로 수정을 요청하고 성공을 알린다") {
            listOf("", "   ").forEach { title ->
                runTest(mainDispatcher) {
                    val id = Arb.kotlinUuid().next()
                    val input = Arb.ddayInput().next().copy(title = title)
                    val updateDDayUseCase = mockk<UpdateDDayUseCase>()
                    coEvery { updateDDayUseCase(parameter = UpdateDDayUseCase.Parameter(id = id, input = input)) } returns Result.success(1)
                    val viewModel = viewModel(id = id, updateDDayUseCase = updateDDayUseCase)

                    viewModel.effect.test {
                        viewModel.update(input = input)

                        awaitItem() shouldBe DDayDetailEffect.UpdateSucceeded
                        expectNoEvents()
                    }
                    coVerify(exactly = 1) { updateDDayUseCase(parameter = UpdateDDayUseCase.Parameter(id = id, input = input)) }
                }
            }
        }

        test("TC-8058eda4-7002-4560-90d2-d83371015b28 수정을 처리하는 동안 진행 상태를 노출하고 끝나면 해제한다") {
            runTest(mainDispatcher) {
                val dday = Arb.dday(isDeleted = false).next()
                val gate = CompletableDeferred<Unit>()
                val updateDDayUseCase = mockk<UpdateDDayUseCase>()
                coEvery { updateDDayUseCase(parameter = any()) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val viewModel = viewModel(dday = flowOf(Result.success(dday)), id = dday.id, updateDDayUseCase = updateDDayUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe DDayDetailUiState.Loading
                    awaitItem() shouldBe DDayDetailUiState.Content(id = dday.id, detail = dday.detail)

                    viewModel.update(input = Arb.ddayInput().next())
                    awaitItem() shouldBe DDayDetailUiState.Content(id = dday.id, detail = dday.detail, isUpdateInProgress = true)

                    gate.complete(Unit)
                    awaitItem() shouldBe DDayDetailUiState.Content(id = dday.id, detail = dday.detail)
                }
            }
        }

        test("TC-84bfc631-b300-415b-a493-c3d60b58af87 수정에 실패하면 안내 없이 진행 상태만 해제하고 다시 수정할 수 있다") {
            runTest(mainDispatcher) {
                val input = Arb.ddayInput().next()
                val updateDDayUseCase = mockk<UpdateDDayUseCase>()
                coEvery { updateDDayUseCase(parameter = any()) } returns Result.failure(IllegalStateException(Arb.string().next()))
                val viewModel = viewModel(updateDDayUseCase = updateDDayUseCase)

                viewModel.effect.test {
                    viewModel.update(input = input)
                    advanceUntilIdle()
                    viewModel.update(input = input)
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 2) { updateDDayUseCase(parameter = any()) }
            }
        }

        test("TC-55d21aa4-9b96-4c9b-be03-ffc5326d5277 숫자가 빈 간격이 있어 수정하지 않으면 반복 간격 입력이 필요함을 한 번 알린다") {
            runTest(mainDispatcher) {
                val updateDDayUseCase = mockk<UpdateDDayUseCase>()
                coEvery { updateDDayUseCase(parameter = any()) } returns Result.failure(DDayRepeatBlankException())
                val viewModel = viewModel(updateDDayUseCase = updateDDayUseCase)

                viewModel.effect.test {
                    viewModel.update(input = Arb.ddayInput().next())

                    awaitItem() shouldBe DDayDetailEffect.RepeatBlank
                    expectNoEvents()
                }
            }
        }

        test("TC-be5db26f-c4e4-4296-b736-96ad9ef9f704 삭제에 성공하면 삭제를 한 번 요청하고 화면을 떠나도록 한 번 알린다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val deleteDDayUseCase = mockk<DeleteDDayUseCase>()
                coEvery { deleteDDayUseCase(parameter = id) } returns Result.success(1)
                val viewModel = viewModel(id = id, deleteDDayUseCase = deleteDDayUseCase)

                viewModel.effect.test {
                    viewModel.delete()

                    awaitItem() shouldBe DDayDetailEffect.DeleteSucceeded
                    expectNoEvents()
                }
                coVerify(exactly = 1) { deleteDDayUseCase(parameter = id) }
            }
        }

        test("TC-2dab4b33-65f9-4c09-8c0b-cda971982bbf 삭제를 처리하는 동안 진행 상태를 노출하고 수정은 요청한다") {
            runTest(mainDispatcher) {
                val dday = Arb.dday(isDeleted = false).next()
                val gate = CompletableDeferred<Unit>()
                val deleteDDayUseCase = mockk<DeleteDDayUseCase>()
                coEvery { deleteDDayUseCase(parameter = dday.id) } coAnswers {
                    gate.await()
                    Result.failure(IllegalStateException(Arb.string().next()))
                }
                val updateDDayUseCase = mockk<UpdateDDayUseCase>()
                coEvery { updateDDayUseCase(parameter = any()) } returns Result.success(1)
                val viewModel =
                    viewModel(
                        dday = flowOf(Result.success(dday)),
                        id = dday.id,
                        updateDDayUseCase = updateDDayUseCase,
                        deleteDDayUseCase = deleteDDayUseCase,
                    )

                viewModel.uiState.test {
                    awaitItem() shouldBe DDayDetailUiState.Loading
                    awaitItem() shouldBe DDayDetailUiState.Content(id = dday.id, detail = dday.detail)

                    viewModel.delete()
                    awaitItem() shouldBe DDayDetailUiState.Content(id = dday.id, detail = dday.detail, isDeleteInProgress = true)

                    viewModel.update(input = Arb.ddayInput().next())
                    runCurrent()
                    coVerify(exactly = 1) { updateDDayUseCase(parameter = any()) }

                    gate.complete(Unit)
                    advanceUntilIdle()
                    viewModel.uiState.value shouldBe DDayDetailUiState.Content(id = dday.id, detail = dday.detail)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-152d3bcd-bb23-4bee-a126-61e8a693f90b 삭제에 실패하면 화면에 머무르고 다시 삭제할 수 있다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val deleteDDayUseCase = mockk<DeleteDDayUseCase>()
                coEvery { deleteDDayUseCase(parameter = id) } returns Result.failure(IllegalStateException(Arb.string().next()))
                val viewModel = viewModel(id = id, deleteDDayUseCase = deleteDDayUseCase)

                viewModel.effect.test {
                    viewModel.delete()
                    advanceUntilIdle()
                    viewModel.delete()
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 2) { deleteDDayUseCase(parameter = id) }
            }
        }

        test("TC-4ef02a1f-ffb0-4b91-91ce-729ed71bf352 내용 수정을 처리하는 동안 내용 수정은 다시 요청하지 않고 삭제는 요청한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val gate = CompletableDeferred<Unit>()
                val updateDDayUseCase = mockk<UpdateDDayUseCase>()
                coEvery { updateDDayUseCase(parameter = any()) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val deleteDDayUseCase = mockk<DeleteDDayUseCase>()
                coEvery { deleteDDayUseCase(parameter = id) } returns Result.failure(IllegalStateException(Arb.string().next()))
                val viewModel = viewModel(id = id, updateDDayUseCase = updateDDayUseCase, deleteDDayUseCase = deleteDDayUseCase)

                viewModel.update(input = Arb.ddayInput().next())
                runCurrent()
                viewModel.update(input = Arb.ddayInput().next())
                viewModel.delete()
                runCurrent()

                coVerify(exactly = 1) { updateDDayUseCase(parameter = any()) }
                coVerify(exactly = 1) { deleteDDayUseCase(parameter = id) }
                gate.complete(Unit)
                advanceUntilIdle()
            }
        }

        test("TC-4ef02a1f-ffb0-4b91-91ce-729ed71bf352 삭제를 처리하는 동안 삭제는 다시 요청하지 않고 내용 수정은 요청한다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val gate = CompletableDeferred<Unit>()
                val deleteDDayUseCase = mockk<DeleteDDayUseCase>()
                coEvery { deleteDDayUseCase(parameter = id) } coAnswers {
                    gate.await()
                    Result.success(1)
                }
                val updateDDayUseCase = mockk<UpdateDDayUseCase>()
                coEvery { updateDDayUseCase(parameter = any()) } returns Result.success(1)
                val viewModel = viewModel(id = id, updateDDayUseCase = updateDDayUseCase, deleteDDayUseCase = deleteDDayUseCase)

                viewModel.delete()
                runCurrent()
                viewModel.delete()
                viewModel.update(input = Arb.ddayInput().next())
                runCurrent()

                coVerify(exactly = 1) { deleteDDayUseCase(parameter = id) }
                coVerify(exactly = 1) { updateDDayUseCase(parameter = any()) }
                gate.complete(Unit)
                advanceUntilIdle()
            }
        }
    }

    private companion object {
        fun viewModel(
            dday: Flow<Result<DDay?>> = emptyFlow(),
            id: Uuid = Arb.kotlinUuid().next(),
            updateDDayUseCase: UpdateDDayUseCase = mockk(),
            deleteDDayUseCase: DeleteDDayUseCase = mockk(),
        ): DDayDetailViewModel {
            val findDDayUseCase = mockk<FindDDayUseCase>()
            every { findDDayUseCase(parameter = id) } returns dday

            return DDayDetailViewModel(
                id = id,
                updateDDayUseCase = updateDDayUseCase,
                deleteDDayUseCase = deleteDDayUseCase,
                findDDayUseCase = findDDayUseCase,
            )
        }
    }
}
