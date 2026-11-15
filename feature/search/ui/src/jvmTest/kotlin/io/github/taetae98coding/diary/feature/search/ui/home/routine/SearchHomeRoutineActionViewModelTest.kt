@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.search.ui.home.routine

import app.cash.turbine.test
import io.github.taetae98coding.diary.compose.routine.RoutineListEffect
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FinishRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestartRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.RestoreRoutineUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
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

class SearchHomeRoutineActionViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-22096372-89f3-41d5-993e-972ad9b5a566 루틴을 완료하면 완료를 요청하고 완료 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val environment = Environment()

                environment.viewModel.effect.test {
                    environment.viewModel.finish(id = id)

                    awaitItem() shouldBe RoutineListEffect.Finished(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { environment.finishRoutineUseCase(parameter = id) }
                coVerify(exactly = 0) { environment.restartRoutineUseCase(parameter = any()) }
            }
        }

        test("TC-22096372-89f3-41d5-993e-972ad9b5a566 루틴을 다시 시작하면 다시 시작을 요청하고 다시 시작 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val environment = Environment()

                environment.viewModel.effect.test {
                    environment.viewModel.restart(id = id)

                    awaitItem() shouldBe RoutineListEffect.Restarted(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { environment.restartRoutineUseCase(parameter = id) }
                coVerify(exactly = 0) { environment.finishRoutineUseCase(parameter = any()) }
            }
        }

        test("TC-52108144-1cad-4c1b-85da-0bca00128799 루틴을 삭제하면 삭제를 요청하고 삭제 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val environment = Environment()

                environment.viewModel.effect.test {
                    environment.viewModel.delete(id = id)

                    awaitItem() shouldBe RoutineListEffect.Deleted(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { environment.deleteRoutineUseCase(parameter = id) }
            }
        }

        test("TC-22b6f8ce-53f1-4b4f-844b-1a462721e10c 루틴의 완료, 다시 시작, 삭제가 저장되지 못하면 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val environment = Environment(result = Result.failure(IllegalStateException()))

                environment.viewModel.effect.test {
                    environment.viewModel.finish(id = id)
                    advanceUntilIdle()
                    environment.viewModel.restart(id = id)
                    advanceUntilIdle()
                    environment.viewModel.delete(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { environment.finishRoutineUseCase(parameter = id) }
                coVerify(exactly = 1) { environment.restartRoutineUseCase(parameter = id) }
                coVerify(exactly = 1) { environment.deleteRoutineUseCase(parameter = id) }
            }
        }

        test("TC-27470dd3-8e88-4047-bf84-f787c9bfd442 루틴 완료를 실행 취소하면 다시 시작을 요청하고 새 안내는 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val environment = Environment()

                environment.viewModel.effect.test {
                    environment.viewModel.undo(effect = RoutineListEffect.Finished(id = id))
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { environment.restartRoutineUseCase(parameter = id) }
                coVerify(exactly = 0) { environment.finishRoutineUseCase(parameter = any()) }
            }
        }

        test("TC-27470dd3-8e88-4047-bf84-f787c9bfd442 루틴 다시 시작을 실행 취소하면 완료를 요청하고 새 안내는 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val environment = Environment()

                environment.viewModel.effect.test {
                    environment.viewModel.undo(effect = RoutineListEffect.Restarted(id = id))
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { environment.finishRoutineUseCase(parameter = id) }
                coVerify(exactly = 0) { environment.restartRoutineUseCase(parameter = any()) }
            }
        }

        test("TC-27470dd3-8e88-4047-bf84-f787c9bfd442 루틴 삭제를 실행 취소하면 복구를 요청하고 새 안내는 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val environment = Environment()

                environment.viewModel.effect.test {
                    environment.viewModel.undo(effect = RoutineListEffect.Deleted(id = id))
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { environment.restoreRoutineUseCase(parameter = id) }
            }
        }
    }

    private class Environment(
        result: Result<Int> = Result.success(1),
    ) {
        val finishRoutineUseCase: FinishRoutineUseCase = mockk { coEvery { this@mockk(parameter = any()) } returns result }
        val restartRoutineUseCase: RestartRoutineUseCase = mockk { coEvery { this@mockk(parameter = any()) } returns result }
        val deleteRoutineUseCase: DeleteRoutineUseCase = mockk { coEvery { this@mockk(parameter = any()) } returns result }
        val restoreRoutineUseCase: RestoreRoutineUseCase = mockk { coEvery { this@mockk(parameter = any()) } returns result }

        val viewModel: SearchHomeRoutineViewModel =
            SearchHomeRoutineViewModel(
                searchRoutineUseCase = mockk(),
                finishRoutineUseCase = finishRoutineUseCase,
                restartRoutineUseCase = restartRoutineUseCase,
                deleteRoutineUseCase = deleteRoutineUseCase,
                restoreRoutineUseCase = restoreRoutineUseCase,
            )
    }
}
