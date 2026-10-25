@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.checklist.ui.add

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.testing.checklist.checklistDetail
import io.github.taetae98coding.diary.domain.checklist.exception.ChecklistTitleBlankException
import io.github.taetae98coding.diary.domain.checklist.usecase.AddChecklistUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
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
import kotlin.uuid.Uuid

class ChecklistAddViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-bc713440-0005-43de-9923-2097f1f33358 추가에 성공하면 입력한 제목, 설명과 항목으로 추가를 요청하고 성공을 한 번 알린다") {
            runTest(mainDispatcher) {
                val parameter = parameter()
                val addChecklistUseCase = mockk<AddChecklistUseCase>()
                coEvery { addChecklistUseCase(parameter = parameter) } returns Result.success(fixtureMonkey.giveMeOne<Uuid>())
                val viewModel = ChecklistAddViewModel(addChecklistUseCase = addChecklistUseCase)

                viewModel.effect.test {
                    viewModel.add(detail = parameter.detail, itemContentList = parameter.itemContentList)

                    awaitItem() shouldBe ChecklistAddEffect.AddSucceeded
                    expectNoEvents()
                }
                coVerify(exactly = 1) { addChecklistUseCase(parameter = parameter) }
            }
        }

        test("TC-49dc8390-b403-4ba5-a76a-853c521be665 추가를 처리하는 동안 진행 상태를 노출하고 끝나면 해제한다") {
            runTest(mainDispatcher) {
                val parameter = parameter()
                val gate = CompletableDeferred<Unit>()
                val addChecklistUseCase = mockk<AddChecklistUseCase>()
                coEvery { addChecklistUseCase(parameter = parameter) } coAnswers {
                    gate.await()
                    Result.success(fixtureMonkey.giveMeOne<Uuid>())
                }
                val viewModel = ChecklistAddViewModel(addChecklistUseCase = addChecklistUseCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe ChecklistAddUiState(isInProgress = false)

                    viewModel.add(detail = parameter.detail, itemContentList = parameter.itemContentList)
                    awaitItem() shouldBe ChecklistAddUiState(isInProgress = true)

                    gate.complete(Unit)
                    awaitItem() shouldBe ChecklistAddUiState(isInProgress = false)
                }
            }
        }

        test("TC-fb408e72-941f-425f-91a2-d88d18702f7f 저장에 실패하면 안내 없이 진행 상태만 해제한다") {
            runTest(mainDispatcher) {
                val parameter = parameter()
                val addChecklistUseCase = mockk<AddChecklistUseCase>()
                coEvery { addChecklistUseCase(parameter = parameter) } returns Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>()))
                val viewModel = ChecklistAddViewModel(addChecklistUseCase = addChecklistUseCase)

                viewModel.effect.test {
                    viewModel.add(detail = parameter.detail, itemContentList = parameter.itemContentList)
                    advanceUntilIdle()

                    expectNoEvents()
                }
                viewModel.uiState.value shouldBe ChecklistAddUiState(isInProgress = false)
            }
        }

        test("TC-e59eaf14-df79-4587-a34f-775b4ffa7492 제목이 비어 있어 추가하지 않으면 제목 입력이 필요함을 한 번 알린다") {
            runTest(mainDispatcher) {
                val parameter = parameter()
                val addChecklistUseCase = mockk<AddChecklistUseCase>()
                coEvery { addChecklistUseCase(parameter = parameter) } returns Result.failure(ChecklistTitleBlankException())
                val viewModel = ChecklistAddViewModel(addChecklistUseCase = addChecklistUseCase)

                viewModel.effect.test {
                    viewModel.add(detail = parameter.detail, itemContentList = parameter.itemContentList)

                    awaitItem() shouldBe ChecklistAddEffect.TitleBlank
                    expectNoEvents()
                }
            }
        }

        test("TC-04d54679-3918-49e7-915a-5216f0623b18 추가를 처리하는 동안 전달된 추가 요청은 처리하지 않는다") {
            runTest(mainDispatcher) {
                val parameter = parameter()
                val gate = CompletableDeferred<Unit>()
                val addChecklistUseCase = mockk<AddChecklistUseCase>()
                coEvery { addChecklistUseCase(parameter = any()) } coAnswers {
                    gate.await()
                    Result.success(fixtureMonkey.giveMeOne<Uuid>())
                }
                val viewModel = ChecklistAddViewModel(addChecklistUseCase = addChecklistUseCase)

                viewModel.add(detail = parameter.detail, itemContentList = parameter.itemContentList)
                runCurrent()
                viewModel.add(detail = parameter.detail, itemContentList = parameter.itemContentList)
                gate.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { addChecklistUseCase(parameter = any()) }
            }
        }

        test("추가가 끝나면 다시 추가를 요청할 수 있다") {
            runTest(mainDispatcher) {
                val parameter = parameter()
                val addChecklistUseCase = mockk<AddChecklistUseCase>()
                coEvery { addChecklistUseCase(parameter = parameter) } returns Result.success(fixtureMonkey.giveMeOne<Uuid>())
                val viewModel = ChecklistAddViewModel(addChecklistUseCase = addChecklistUseCase)

                viewModel.add(detail = parameter.detail, itemContentList = parameter.itemContentList)
                advanceUntilIdle()
                viewModel.add(detail = parameter.detail, itemContentList = parameter.itemContentList)
                advanceUntilIdle()

                coVerify(exactly = 2) { addChecklistUseCase(parameter = parameter) }
            }
        }
    }

    private companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private const val ITEM_COUNT = 3

        private fun parameter(): AddChecklistUseCase.Parameter =
            AddChecklistUseCase.Parameter(
                detail = fixtureMonkey.checklistDetail(),
                itemContentList = List(ITEM_COUNT) { index -> "item-$index-" + fixtureMonkey.giveMeOne<String>() },
            )
    }
}
