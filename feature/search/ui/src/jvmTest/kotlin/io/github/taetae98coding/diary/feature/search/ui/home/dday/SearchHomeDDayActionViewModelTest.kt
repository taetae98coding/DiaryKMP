@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.search.ui.home.dday

import app.cash.turbine.test
import io.github.taetae98coding.diary.compose.dday.DDayListEffect
import io.github.taetae98coding.diary.domain.dday.usecase.DeleteDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.RestoreDDayUseCase
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

class SearchHomeDDayActionViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-52108144-1cad-4c1b-85da-0bca00128799 디데이를 삭제하면 삭제를 요청하고 삭제 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val environment = Environment()

                environment.viewModel.effect.test {
                    environment.viewModel.delete(id = id)

                    awaitItem() shouldBe DDayListEffect.Deleted(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { environment.deleteDDayUseCase(parameter = id) }
            }
        }

        test("TC-22b6f8ce-53f1-4b4f-844b-1a462721e10c 디데이 삭제가 저장되지 못하면 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val environment = Environment(result = Result.failure(IllegalStateException()))

                environment.viewModel.effect.test {
                    environment.viewModel.delete(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-27470dd3-8e88-4047-bf84-f787c9bfd442 디데이 삭제를 실행 취소하면 복구를 요청하고 새 안내는 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = Arb.kotlinUuid().next()
                val environment = Environment()

                environment.viewModel.effect.test {
                    environment.viewModel.restore(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { environment.restoreDDayUseCase(parameter = id) }
            }
        }
    }

    private class Environment(
        result: Result<Int> = Result.success(1),
    ) {
        val deleteDDayUseCase: DeleteDDayUseCase = mockk { coEvery { this@mockk(parameter = any()) } returns result }
        val restoreDDayUseCase: RestoreDDayUseCase = mockk { coEvery { this@mockk(parameter = any()) } returns result }

        val viewModel: SearchHomeDDayViewModel =
            SearchHomeDDayViewModel(
                searchDDayUseCase = mockk(),
                deleteDDayUseCase = deleteDDayUseCase,
                restoreDDayUseCase = restoreDDayUseCase,
            )
    }
}
