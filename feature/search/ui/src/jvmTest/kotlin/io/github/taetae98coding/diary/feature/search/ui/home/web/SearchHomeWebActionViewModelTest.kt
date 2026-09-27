@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.search.ui.home.web

import app.cash.turbine.test
import io.github.taetae98coding.diary.compose.web.WebListEffect
import io.github.taetae98coding.diary.domain.web.usecase.DeleteWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.RestoreWebUseCase
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

class SearchHomeWebActionViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-SEARCH-HOME-FEATURE-032 웹 항목을 삭제하면 삭제를 요청하고 삭제 안내를 한 번 보낸다") {
            runTest(mainDispatcher) {
                val id = searchId()
                val environment = Environment()

                environment.viewModel.effect.test {
                    environment.viewModel.delete(id = id)

                    awaitItem() shouldBe WebListEffect.Deleted(id = id)
                    expectNoEvents()
                }
                coVerify(exactly = 1) { environment.deleteWebUseCase(parameter = id) }
            }
        }

        test("TC-SEARCH-HOME-FEATURE-036 웹 항목 삭제가 저장되지 못하면 안내를 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = searchId()
                val environment = Environment(result = Result.failure(IllegalStateException()))

                environment.viewModel.effect.test {
                    environment.viewModel.delete(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("TC-SEARCH-HOME-FEATURE-033 웹 항목 삭제를 실행 취소하면 복구를 요청하고 새 안내는 보내지 않는다") {
            runTest(mainDispatcher) {
                val id = searchId()
                val environment = Environment()

                environment.viewModel.effect.test {
                    environment.viewModel.restore(id = id)
                    advanceUntilIdle()

                    expectNoEvents()
                }
                coVerify(exactly = 1) { environment.restoreWebUseCase(parameter = id) }
            }
        }
    }

    private class Environment(
        result: Result<Int> = Result.success(1),
    ) {
        val deleteWebUseCase: DeleteWebUseCase = mockk { coEvery { this@mockk(parameter = any()) } returns result }
        val restoreWebUseCase: RestoreWebUseCase = mockk { coEvery { this@mockk(parameter = any()) } returns result }

        val viewModel: SearchHomeWebViewModel =
            SearchHomeWebViewModel(
                searchWebUseCase = mockk(),
                deleteWebUseCase = deleteWebUseCase,
                restoreWebUseCase = restoreWebUseCase,
            )
    }
}
