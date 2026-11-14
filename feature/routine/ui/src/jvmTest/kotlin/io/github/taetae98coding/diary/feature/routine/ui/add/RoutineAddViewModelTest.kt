@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.routine.ui.add

import app.cash.turbine.test
import io.github.taetae98coding.diary.domain.routine.exception.RoutineAmountPerRecordInvalidException
import io.github.taetae98coding.diary.domain.routine.exception.RoutineGoalAmountInvalidException
import io.github.taetae98coding.diary.domain.routine.exception.RoutineTitleBlankException
import io.github.taetae98coding.diary.domain.routine.usecase.AddRoutineUseCase
import io.github.taetae98coding.diary.feature.routine.ui.form.routineInput
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class RoutineAddViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-81afe219-36ea-43b0-89c5-e04122c6fdcd 추가에 성공하면 입력으로 추가를 한 번 요청하고 성공을 한 번 알린다") {
            runTest(mainDispatcher) {
                val parameter = parameter()
                val useCase = mockk<AddRoutineUseCase>()
                coEvery { useCase(parameter = parameter) } returns Result.success(Arb.kotlinUuid().next())
                val viewModel = RoutineAddViewModel(addRoutineUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(input = parameter.input, tagIdSet = parameter.tagIdSet)

                    awaitItem() shouldBe RoutineAddEffect.AddSucceeded
                    expectNoEvents()
                }
                coVerify(exactly = 1) { useCase(parameter = parameter) }
            }
        }

        test("TC-a8321e69-e09f-442f-b9b0-c0735f920433 고른 태그를 새 루틴과 함께 추가하도록 넘긴다") {
            runTest(mainDispatcher) {
                val parameter = parameter().copy(tagIdSet = setOf(Arb.kotlinUuid().next(), Arb.kotlinUuid().next()))
                val useCase = mockk<AddRoutineUseCase>()
                coEvery { useCase(parameter = any()) } returns Result.success(Arb.kotlinUuid().next())
                val viewModel = RoutineAddViewModel(addRoutineUseCase = useCase)

                viewModel.add(input = parameter.input, tagIdSet = parameter.tagIdSet)
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(parameter = parameter) }
            }
        }

        test("TC-4a4d3211-4f99-401b-9c45-4d424331a656 추가를 처리하는 동안 진행 상태를 노출하고 끝나면 해제한다") {
            runTest(mainDispatcher) {
                val parameter = parameter()
                val gate = CompletableDeferred<Unit>()
                val useCase = mockk<AddRoutineUseCase>()
                coEvery { useCase(parameter = parameter) } coAnswers {
                    gate.await()
                    Result.success(Arb.kotlinUuid().next())
                }
                val viewModel = RoutineAddViewModel(addRoutineUseCase = useCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe RoutineAddUiState(isInProgress = false)

                    viewModel.add(input = parameter.input, tagIdSet = parameter.tagIdSet)
                    awaitItem() shouldBe RoutineAddUiState(isInProgress = true)

                    gate.complete(Unit)
                    awaitItem() shouldBe RoutineAddUiState(isInProgress = false)
                }
            }
        }

        test("TC-73fc3bf2-1850-45f0-8332-cfeec6c06d3c 저장에 실패하면 안내 없이 진행 상태만 해제하고 다시 추가할 수 있다") {
            runTest(mainDispatcher) {
                val parameter = parameter()
                val useCase = mockk<AddRoutineUseCase>()
                coEvery { useCase(parameter = parameter) } returns Result.failure(IllegalStateException(Arb.string().next()))
                val viewModel = RoutineAddViewModel(addRoutineUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(input = parameter.input, tagIdSet = parameter.tagIdSet)
                    advanceUntilIdle()
                    viewModel.uiState.value shouldBe RoutineAddUiState(isInProgress = false)
                    viewModel.add(input = parameter.input, tagIdSet = parameter.tagIdSet)
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 2) { useCase(parameter = parameter) }
            }
        }

        test("TC-f71ab5c3-e335-4369-a9fb-42f80556a83c 제목이 비어 있어 추가하지 않으면 제목 입력이 필요함을 한 번 알린다") {
            runTest(mainDispatcher) {
                assertRejected(throwable = RoutineTitleBlankException(), effect = RoutineAddEffect.TitleBlank)
            }
        }

        test("TC-2c47d77d-860b-4a65-a38d-fcf76ae11be1 목표량이 0이어서 추가하지 않으면 목표량 입력이 필요함을 한 번 알린다") {
            runTest(mainDispatcher) {
                assertRejected(throwable = RoutineGoalAmountInvalidException(), effect = RoutineAddEffect.GoalAmountInvalid)
            }
        }

        test("TC-335277a4-90f6-4feb-a974-6cca8254ecfb 한 번에 기록할 양이 없어 추가하지 않으면 그 입력이 필요함을 한 번 알린다") {
            runTest(mainDispatcher) {
                assertRejected(throwable = RoutineAmountPerRecordInvalidException(), effect = RoutineAddEffect.AmountPerRecordInvalid)
            }
        }

        test("TC-d96c125b-abf2-4da5-becd-2845439980ab 추가를 처리하는 동안 전달된 추가 요청은 처리하지 않는다") {
            runTest(mainDispatcher) {
                val parameter = parameter()
                val gate = CompletableDeferred<Unit>()
                val useCase = mockk<AddRoutineUseCase>()
                coEvery { useCase(parameter = any()) } coAnswers {
                    gate.await()
                    Result.success(Arb.kotlinUuid().next())
                }
                val viewModel = RoutineAddViewModel(addRoutineUseCase = useCase)

                viewModel.add(input = parameter.input, tagIdSet = parameter.tagIdSet)
                runCurrent()
                viewModel.add(input = parameter.input, tagIdSet = parameter.tagIdSet)
                gate.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(parameter = any()) }
            }
        }

        test("추가가 끝나면 다시 추가를 요청할 수 있다") {
            runTest(mainDispatcher) {
                val parameter = parameter()
                val useCase = mockk<AddRoutineUseCase>()
                coEvery { useCase(parameter = parameter) } returns Result.success(Arb.kotlinUuid().next())
                val viewModel = RoutineAddViewModel(addRoutineUseCase = useCase)

                viewModel.add(input = parameter.input, tagIdSet = parameter.tagIdSet)
                advanceUntilIdle()
                viewModel.add(input = parameter.input, tagIdSet = parameter.tagIdSet)
                advanceUntilIdle()

                coVerify(exactly = 2) { useCase(parameter = parameter) }
            }
        }
    }

    private companion object {
        fun parameter(): AddRoutineUseCase.Parameter = AddRoutineUseCase.Parameter(input = Arb.routineInput().next(), tagIdSet = emptySet())

        suspend fun assertRejected(
            throwable: Throwable,
            effect: RoutineAddEffect,
        ) {
            val parameter = parameter()
            val useCase = mockk<AddRoutineUseCase>()
            coEvery { useCase(parameter = parameter) } returns Result.failure(throwable)
            val viewModel = RoutineAddViewModel(addRoutineUseCase = useCase)

            viewModel.effect.test {
                viewModel.add(input = parameter.input, tagIdSet = parameter.tagIdSet)

                awaitItem() shouldBe effect
                expectNoEvents()
            }
        }
    }
}
