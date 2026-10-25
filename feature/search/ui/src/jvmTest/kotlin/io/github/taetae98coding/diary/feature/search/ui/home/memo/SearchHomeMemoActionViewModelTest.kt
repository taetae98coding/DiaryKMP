@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.search.ui.home.memo

import app.cash.turbine.test
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.feature.search.ui.home.searchId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

class SearchHomeMemoActionViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-59805e68-ece8-4b40-9ff0-9fb81549022d 미완료 메모를 완료하면 완료를 요청하고 완료 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = searchId()
                val environment = Environment()

                environment.viewModel.effect.test {
                    environment.viewModel.finish(id = id)

                    awaitItem() shouldBe SearchHomeMemoEffect.Finished(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { environment.finishMemoUseCase(parameter = id) }
            }
        }

        test("TC-59805e68-ece8-4b40-9ff0-9fb81549022d 완료된 메모를 다시 시작하면 다시 시작을 요청하고 다시 시작 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = searchId()
                val environment = Environment()

                environment.viewModel.effect.test {
                    environment.viewModel.restart(id = id)

                    awaitItem() shouldBe SearchHomeMemoEffect.Restarted(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { environment.restartMemoUseCase(parameter = id) }
            }
        }

        test("TC-52108144-1cad-4c1b-85da-0bca00128799 메모를 삭제하면 삭제를 요청하고 삭제 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = searchId()
                val environment = Environment()

                environment.viewModel.effect.test {
                    environment.viewModel.delete(id = id)

                    awaitItem() shouldBe SearchHomeMemoEffect.Deleted(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { environment.deleteMemoUseCase(parameter = id) }
            }
        }

        test("TC-22b6f8ce-53f1-4b4f-844b-1a462721e10c 메모의 동작이 저장되지 못하면 안내를 보내지 않는다") {
            val actionList: List<(SearchHomeMemoViewModel, Uuid) -> Unit> =
                listOf(
                    { viewModel, id -> viewModel.finish(id = id) },
                    { viewModel, id -> viewModel.restart(id = id) },
                    { viewModel, id -> viewModel.delete(id = id) },
                )

            actionList.forEach { action ->
                runTest(mainDispatcher) {
                    val id = searchId()
                    val environment = Environment(result = Result.failure(IllegalStateException()))

                    environment.viewModel.effect.test {
                        action(environment.viewModel, id)
                        advanceUntilIdle()

                        expectNoEvents()
                    }
                }
            }
        }

        test("TC-27470dd3-8e88-4047-bf84-f787c9bfd442 메모 완료를 실행 취소하면 다시 시작을 요청하고 새 안내는 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = searchId()
                val environment = Environment()

                environment.viewModel.effect.test {
                    environment.viewModel.undo(effect = SearchHomeMemoEffect.Finished(id = id))
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { environment.restartMemoUseCase(parameter = id) }
                coVerify(exactly = 0) { environment.finishMemoUseCase(parameter = any()) }
            }
        }

        test("TC-27470dd3-8e88-4047-bf84-f787c9bfd442 메모 다시 시작을 실행 취소하면 완료를 요청하고 새 안내는 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = searchId()
                val environment = Environment()

                environment.viewModel.effect.test {
                    environment.viewModel.undo(effect = SearchHomeMemoEffect.Restarted(id = id))
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { environment.finishMemoUseCase(parameter = id) }
                coVerify(exactly = 0) { environment.restartMemoUseCase(parameter = any()) }
            }
        }

        test("TC-27470dd3-8e88-4047-bf84-f787c9bfd442 메모 삭제를 실행 취소하면 복구를 요청하고 새 안내는 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = searchId()
                val environment = Environment()

                environment.viewModel.effect.test {
                    environment.viewModel.undo(effect = SearchHomeMemoEffect.Deleted(id = id))
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { environment.restoreMemoUseCase(parameter = id) }
            }
        }
    }

    private class Environment(
        result: Result<Int> = Result.success(1),
    ) {
        val finishMemoUseCase: FinishMemoUseCase = mockk { coEvery { this@mockk(parameter = any()) } returns result }
        val restartMemoUseCase: RestartMemoUseCase = mockk { coEvery { this@mockk(parameter = any()) } returns result }
        val deleteMemoUseCase: DeleteMemoUseCase = mockk { coEvery { this@mockk(parameter = any()) } returns result }
        val restoreMemoUseCase: RestoreMemoUseCase = mockk { coEvery { this@mockk(parameter = any()) } returns result }

        val viewModel: SearchHomeMemoViewModel =
            SearchHomeMemoViewModel(
                searchMemoUseCase = mockk(),
                finishMemoUseCase = finishMemoUseCase,
                restartMemoUseCase = restartMemoUseCase,
                deleteMemoUseCase = deleteMemoUseCase,
                restoreMemoUseCase = restoreMemoUseCase,
            )
    }
}
