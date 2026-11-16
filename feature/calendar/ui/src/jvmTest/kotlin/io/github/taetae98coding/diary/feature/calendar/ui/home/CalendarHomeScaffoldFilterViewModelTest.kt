@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.calendar.ui.home

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.memo.usecase.GetCalendarFilterUseCase
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class CalendarHomeScaffoldFilterViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-a5c69eb9-d97f-45a6-b738-838199170318 선택한 태그가 있으면 필터 적용 상태를 노출한다") {
            runTest(mainDispatcher) {
                val selectedTagList = listOf(tag())
                val viewModel = filterViewModel(flow = flowOf(Result.success(selectedTagList)))

                viewModel.uiState.test {
                    awaitItem() shouldBe CalendarHomeScaffoldFilterUiState()
                    advanceUntilIdle()
                    awaitItem() shouldBe CalendarHomeScaffoldFilterUiState(isApplied = true)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("TC-a5c69eb9-d97f-45a6-b738-838199170318 선택한 태그가 없으면 필터 적용 상태를 노출하지 않는다") {
            runTest(mainDispatcher) {
                val viewModel = filterViewModel(flow = flowOf(Result.success(emptyList())))

                viewModel.uiState.test {
                    awaitItem() shouldBe CalendarHomeScaffoldFilterUiState()
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("필터 조회에 실패하면 적용되지 않은 상태를 노출한다") {
            runTest(mainDispatcher) {
                val viewModel = filterViewModel(flow = flowOf(Result.failure(IllegalStateException())))

                viewModel.uiState.test {
                    awaitItem() shouldBe CalendarHomeScaffoldFilterUiState()
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }
    }
}

private fun filterViewModel(flow: Flow<Result<List<Tag>>>): CalendarHomeScaffoldFilterViewModel {
    val getCalendarFilterUseCase = mockk<GetCalendarFilterUseCase>()
    every { getCalendarFilterUseCase() } returns flow

    return CalendarHomeScaffoldFilterViewModel(getCalendarFilterUseCase = getCalendarFilterUseCase)
}

private fun tag(): Tag =
    Arb
        .tag()
        .next()
        .copy(
            updatedAt = Arb.secondPrecisionInstant().next(),
            createdAt = Arb.secondPrecisionInstant().next(),
        )
