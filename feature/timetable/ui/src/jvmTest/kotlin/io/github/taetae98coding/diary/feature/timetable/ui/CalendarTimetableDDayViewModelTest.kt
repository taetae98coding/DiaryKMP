package io.github.taetae98coding.diary.feature.timetable.ui

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.dday.CalendarDDay
import io.github.taetae98coding.diary.core.testing.dday.calendarDDay
import io.github.taetae98coding.diary.domain.dday.usecase.GetCalendarDDayUseCase
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange

class CalendarTimetableDDayViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-9a2e5586-bd97-4581-879b-cad1d9d7b1a9 디데이 조회 기간은 메모 조회 기간과 같다") {
            val caseList =
                listOf(
                    september(day = 23)..september(day = 23) to september(day = 22)..september(day = 24),
                    september(day = 20)..september(day = 26) to september(day = 13)..LocalDate(year = 2026, month = 10, day = 3),
                )

            caseList.forEach { (dateRange, expected) ->
                runTest(mainDispatcher) {
                    val useCase = getCalendarDDayUseCase()
                    val viewModel = CalendarTimetableDDayViewModel(getCalendarDDayUseCase = useCase)

                    viewModel.uiState.test {
                        awaitItem()
                        viewModel.fetch(dateRange = dateRange)
                        advanceUntilIdle()

                        verify(exactly = 1) { useCase(parameter = expected) }
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        test("TC-434e6a06-f027-4746-b308-04534fec2acf 날짜를 옮기면 옮긴 날짜의 조회 기간으로 디데이를 다시 조회한다") {
            runTest(mainDispatcher) {
                val firstDDay = Arb.calendarDDay().next()
                val movedDDay = Arb.calendarDDay().next()
                val useCase =
                    getCalendarDDayUseCase(
                        september(day = 22)..september(day = 24) to Result.success(listOf(firstDDay)),
                        september(day = 23)..september(day = 25) to Result.success(listOf(movedDDay)),
                    )
                val viewModel = CalendarTimetableDDayViewModel(getCalendarDDayUseCase = useCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe CalendarTimetableDDayUiState()
                    viewModel.fetch(dateRange = september(day = 23)..september(day = 23))
                    awaitItem() shouldBe CalendarTimetableDDayUiState(ddayList = listOf(firstDDay))

                    viewModel.fetch(dateRange = september(day = 24)..september(day = 24))

                    awaitItem() shouldBe CalendarTimetableDDayUiState(ddayList = listOf(movedDDay))
                }

                verify(exactly = 1) { useCase(parameter = september(day = 23)..september(day = 25)) }
            }
        }

        test("TC-bbf93916-0c01-4b20-8cd0-f508823c2b89 디데이 조회에 실패하면 디데이 없이 제공한다") {
            runTest(mainDispatcher) {
                val useCase = mockk<GetCalendarDDayUseCase>()
                every { useCase(parameter = any()) } returns flowOf(Result.failure(IllegalStateException(Arb.nonBlankString().next())))
                val viewModel = CalendarTimetableDDayViewModel(getCalendarDDayUseCase = useCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe CalendarTimetableDDayUiState()
                    viewModel.fetch(dateRange = september(day = 24)..september(day = 24))
                    advanceUntilIdle()

                    expectNoEvents()
                }
            }
        }

        test("날짜를 지정하기 전에는 디데이를 조회하지 않는다") {
            runTest(mainDispatcher) {
                val useCase = getCalendarDDayUseCase()
                val viewModel = CalendarTimetableDDayViewModel(getCalendarDDayUseCase = useCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe CalendarTimetableDDayUiState()
                    advanceUntilIdle()
                    expectNoEvents()
                }

                verify(exactly = 0) { useCase(parameter = any()) }
            }
        }
    }

    private fun getCalendarDDayUseCase(vararg resultByRange: Pair<LocalDateRange, Result<List<CalendarDDay>>>): GetCalendarDDayUseCase =
        mockk<GetCalendarDDayUseCase>().also { useCase ->
            every { useCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
            resultByRange.forEach { (dateRange, result) ->
                every { useCase(parameter = dateRange) } returns flowOf(result)
            }
        }

    private fun september(day: Int): LocalDate = LocalDate(year = 2026, month = 9, day = day)
}
