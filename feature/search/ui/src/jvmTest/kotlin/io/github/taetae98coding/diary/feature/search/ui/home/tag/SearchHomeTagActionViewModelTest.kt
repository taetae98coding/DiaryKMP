@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.search.ui.home.tag

import app.cash.turbine.test
import io.github.taetae98coding.diary.compose.tag.list.TagListEffect
import io.github.taetae98coding.diary.domain.tag.usecase.DeleteTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.FinishTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RestartTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RestoreTagUseCase
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

class SearchHomeTagActionViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-e1cb79a7-62eb-4e64-8242-f9f62fe382bd 미완료 태그를 완료하면 완료를 요청하고 완료 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = searchId()
                val environment = Environment()

                environment.viewModel.effect.test {
                    environment.viewModel.finish(id = id)

                    awaitItem() shouldBe TagListEffect.Finished(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { environment.finishTagUseCase(parameter = id) }
            }
        }

        test("TC-e1cb79a7-62eb-4e64-8242-f9f62fe382bd 완료된 태그를 다시 시작하면 다시 시작을 요청하고 다시 시작 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = searchId()
                val environment = Environment()

                environment.viewModel.effect.test {
                    environment.viewModel.restart(id = id)

                    awaitItem() shouldBe TagListEffect.Restarted(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { environment.restartTagUseCase(parameter = id) }
            }
        }

        test("TC-52108144-1cad-4c1b-85da-0bca00128799 태그를 삭제하면 삭제를 요청하고 삭제 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = searchId()
                val environment = Environment()

                environment.viewModel.effect.test {
                    environment.viewModel.delete(id = id)

                    awaitItem() shouldBe TagListEffect.Deleted(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { environment.deleteTagUseCase(parameter = id) }
            }
        }

        test("TC-22b6f8ce-53f1-4b4f-844b-1a462721e10c 태그의 동작이 저장되지 못하면 안내를 보내지 않는다") {
            val actionList: List<(SearchHomeTagViewModel, Uuid) -> Unit> =
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

        test("TC-27470dd3-8e88-4047-bf84-f787c9bfd442 태그 완료를 실행 취소하면 다시 시작을 요청하고 새 안내는 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = searchId()
                val environment = Environment()

                environment.viewModel.effect.test {
                    environment.viewModel.undo(effect = TagListEffect.Finished(id = id))
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { environment.restartTagUseCase(parameter = id) }
                coVerify(exactly = 0) { environment.finishTagUseCase(parameter = any()) }
            }
        }

        test("TC-27470dd3-8e88-4047-bf84-f787c9bfd442 태그 다시 시작을 실행 취소하면 완료를 요청하고 새 안내는 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = searchId()
                val environment = Environment()

                environment.viewModel.effect.test {
                    environment.viewModel.undo(effect = TagListEffect.Restarted(id = id))
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { environment.finishTagUseCase(parameter = id) }
                coVerify(exactly = 0) { environment.restartTagUseCase(parameter = any()) }
            }
        }

        test("TC-27470dd3-8e88-4047-bf84-f787c9bfd442 태그 삭제를 실행 취소하면 복구를 요청하고 새 안내는 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = searchId()
                val environment = Environment()

                environment.viewModel.effect.test {
                    environment.viewModel.undo(effect = TagListEffect.Deleted(id = id))
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { environment.restoreTagUseCase(parameter = id) }
            }
        }
    }

    private class Environment(
        result: Result<Int> = Result.success(1),
    ) {
        val finishTagUseCase: FinishTagUseCase = mockk { coEvery { this@mockk(parameter = any()) } returns result }
        val restartTagUseCase: RestartTagUseCase = mockk { coEvery { this@mockk(parameter = any()) } returns result }
        val deleteTagUseCase: DeleteTagUseCase = mockk { coEvery { this@mockk(parameter = any()) } returns result }
        val restoreTagUseCase: RestoreTagUseCase = mockk { coEvery { this@mockk(parameter = any()) } returns result }

        val viewModel: SearchHomeTagViewModel =
            SearchHomeTagViewModel(
                searchTagUseCase = mockk(),
                finishTagUseCase = finishTagUseCase,
                restartTagUseCase = restartTagUseCase,
                deleteTagUseCase = deleteTagUseCase,
                restoreTagUseCase = restoreTagUseCase,
            )
    }
}
