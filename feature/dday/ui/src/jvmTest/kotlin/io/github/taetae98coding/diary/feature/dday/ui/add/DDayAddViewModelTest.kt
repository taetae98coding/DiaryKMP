@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.dday.ui.add

import app.cash.turbine.test
import io.github.taetae98coding.diary.domain.dday.exception.DDayRepeatBlankException
import io.github.taetae98coding.diary.domain.dday.exception.DDayTitleBlankException
import io.github.taetae98coding.diary.domain.dday.usecase.AddDDayUseCase
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayInput
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

class DDayAddViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-7111288b-02d3-460e-9537-e6d873d9a098 추가에 성공하면 입력으로 추가를 한 번 요청하고 성공을 한 번 알린다") {
            runTest(mainDispatcher) {
                val parameter = parameter()
                val useCase = mockk<AddDDayUseCase>()
                coEvery { useCase(parameter = parameter) } returns Result.success(Arb.kotlinUuid().next())
                val viewModel = DDayAddViewModel(addDDayUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(input = parameter.input, tagIdSet = parameter.tagIdSet)

                    awaitItem() shouldBe DDayAddEffect.AddSucceeded
                    expectNoEvents()
                }
                coVerify(exactly = 1) { useCase(parameter = parameter) }
            }
        }

        test("TC-961d5298-db4b-4239-94ed-9c714f597575 고른 태그를 새 디데이와 함께 추가하도록 넘긴다") {
            runTest(mainDispatcher) {
                val parameter = parameter().copy(tagIdSet = setOf(Arb.kotlinUuid().next(), Arb.kotlinUuid().next()))
                val useCase = mockk<AddDDayUseCase>()
                coEvery { useCase(parameter = any()) } returns Result.success(Arb.kotlinUuid().next())
                val viewModel = DDayAddViewModel(addDDayUseCase = useCase)

                viewModel.add(input = parameter.input, tagIdSet = parameter.tagIdSet)
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(parameter = parameter) }
            }
        }

        test("TC-e1f5c8d7-8a37-41ec-9f30-b9b9e03a90c3 추가를 처리하는 동안 진행 상태를 노출하고 끝나면 해제한다") {
            runTest(mainDispatcher) {
                val parameter = parameter()
                val gate = CompletableDeferred<Unit>()
                val useCase = mockk<AddDDayUseCase>()
                coEvery { useCase(parameter = parameter) } coAnswers {
                    gate.await()
                    Result.success(Arb.kotlinUuid().next())
                }
                val viewModel = DDayAddViewModel(addDDayUseCase = useCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe DDayAddUiState(isInProgress = false)

                    viewModel.add(input = parameter.input, tagIdSet = parameter.tagIdSet)
                    awaitItem() shouldBe DDayAddUiState(isInProgress = true)

                    gate.complete(Unit)
                    awaitItem() shouldBe DDayAddUiState(isInProgress = false)
                }
            }
        }

        test("TC-bec3b684-290d-416a-aae4-7c0dfc96e733 저장에 실패하면 안내 없이 진행 상태만 해제하고 다시 추가할 수 있다") {
            runTest(mainDispatcher) {
                val parameter = parameter()
                val useCase = mockk<AddDDayUseCase>()
                coEvery { useCase(parameter = parameter) } returns Result.failure(IllegalStateException(Arb.string().next()))
                val viewModel = DDayAddViewModel(addDDayUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(input = parameter.input, tagIdSet = parameter.tagIdSet)
                    advanceUntilIdle()
                    viewModel.uiState.value shouldBe DDayAddUiState(isInProgress = false)
                    viewModel.add(input = parameter.input, tagIdSet = parameter.tagIdSet)
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 2) { useCase(parameter = parameter) }
            }
        }

        test("TC-564c7cda-ef2e-4e2c-b11e-ee155493a9c9 제목이 비어 있어 추가하지 않으면 제목 입력이 필요함을 한 번 알린다") {
            runTest(mainDispatcher) {
                val parameter = parameter()
                val useCase = mockk<AddDDayUseCase>()
                coEvery { useCase(parameter = parameter) } returns Result.failure(DDayTitleBlankException())
                val viewModel = DDayAddViewModel(addDDayUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(input = parameter.input, tagIdSet = parameter.tagIdSet)

                    awaitItem() shouldBe DDayAddEffect.TitleBlank
                    expectNoEvents()
                }
            }
        }

        test("TC-d41f448c-17eb-496c-8c34-5f2d75d50f9c 숫자가 빈 간격이 있어 추가하지 않으면 반복 간격 입력이 필요함을 한 번 알린다") {
            runTest(mainDispatcher) {
                val parameter = parameter()
                val useCase = mockk<AddDDayUseCase>()
                coEvery { useCase(parameter = parameter) } returns Result.failure(DDayRepeatBlankException())
                val viewModel = DDayAddViewModel(addDDayUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(input = parameter.input, tagIdSet = parameter.tagIdSet)

                    awaitItem() shouldBe DDayAddEffect.RepeatBlank
                    expectNoEvents()
                }
            }
        }

        test("TC-3daa0e79-e5b5-4ee3-8fe3-055f799be018 추가를 처리하는 동안 전달된 추가 요청은 처리하지 않는다") {
            runTest(mainDispatcher) {
                val parameter = parameter()
                val gate = CompletableDeferred<Unit>()
                val useCase = mockk<AddDDayUseCase>()
                coEvery { useCase(parameter = any()) } coAnswers {
                    gate.await()
                    Result.success(Arb.kotlinUuid().next())
                }
                val viewModel = DDayAddViewModel(addDDayUseCase = useCase)

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
                val useCase = mockk<AddDDayUseCase>()
                coEvery { useCase(parameter = parameter) } returns Result.success(Arb.kotlinUuid().next())
                val viewModel = DDayAddViewModel(addDDayUseCase = useCase)

                viewModel.add(input = parameter.input, tagIdSet = parameter.tagIdSet)
                advanceUntilIdle()
                viewModel.add(input = parameter.input, tagIdSet = parameter.tagIdSet)
                advanceUntilIdle()

                coVerify(exactly = 2) { useCase(parameter = parameter) }
            }
        }
    }

    private companion object {
        fun parameter(): AddDDayUseCase.Parameter = AddDDayUseCase.Parameter(input = Arb.ddayInput().next(), tagIdSet = emptySet())
    }
}
