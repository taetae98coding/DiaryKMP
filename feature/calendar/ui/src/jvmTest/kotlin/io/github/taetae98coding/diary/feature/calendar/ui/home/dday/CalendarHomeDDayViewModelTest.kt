@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.calendar.ui.home.dday

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.dday.CalendarDDay
import io.github.taetae98coding.diary.core.testing.dday.calendarDDay
import io.github.taetae98coding.diary.domain.dday.usecase.GetCalendarDDayUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth

class CalendarHomeDDayViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-26e80f20-a148-44f9-8c1a-517b2d6ba172 화면이 표시되면 두 달 전 1일부터 두 달 후 마지막 날까지의 디데이를 조회한다") {
            runTest(mainDispatcher) {
                val ddayList = listOf(Arb.calendarDDay().next(), Arb.calendarDDay().next())
                val useCase = getCalendarDDayUseCase(JULY_FETCH_RANGE to Result.success(ddayList))
                val viewModel = CalendarHomeDDayViewModel(getCalendarDDayUseCase = useCase)

                viewModel.fetch(YearMonth(year = 2026, month = Month.JULY))

                viewModel.uiState.test {
                    awaitItem() shouldBe CalendarHomeDDayUiState()
                    awaitItem() shouldBe CalendarHomeDDayUiState(ddayList = ddayList)
                }

                verify(exactly = 1) { useCase(parameter = JULY_FETCH_RANGE) }
            }
        }

        test("TC-42f2fe42-1dd1-49ae-990e-0fead7b45fb6 표시 중인 달이 바뀌면 이동한 달 기준의 조회 기간으로 디데이를 다시 조회한다") {
            runTest(mainDispatcher) {
                val spring = Arb.calendarDDay().next().copy(date = LocalDate(year = 2026, month = Month.MAY, day = 2))
                val winter = Arb.calendarDDay().next().copy(date = LocalDate(year = 2026, month = Month.NOVEMBER, day = 15))
                val useCase =
                    getCalendarDDayUseCase(
                        JULY_FETCH_RANGE to Result.success(listOf(spring)),
                        SEPTEMBER_FETCH_RANGE to Result.success(listOf(winter)),
                    )
                val viewModel = CalendarHomeDDayViewModel(getCalendarDDayUseCase = useCase)

                viewModel.fetch(YearMonth(year = 2026, month = Month.JULY))

                viewModel.uiState.test {
                    awaitItem() shouldBe CalendarHomeDDayUiState()
                    awaitItem() shouldBe CalendarHomeDDayUiState(ddayList = listOf(spring))

                    viewModel.fetch(YearMonth(year = 2026, month = Month.SEPTEMBER))

                    awaitItem() shouldBe CalendarHomeDDayUiState(ddayList = listOf(winter))
                }

                verify(exactly = 1) { useCase(parameter = SEPTEMBER_FETCH_RANGE) }
            }
        }

        test("TC-b1af5c1b-2acd-4c8b-bc61-b11928087a8d 디데이 조회가 실패하면 디데이 없이 표시한다") {
            runTest(mainDispatcher) {
                val useCase = mockk<GetCalendarDDayUseCase>()
                every { useCase(parameter = any()) } returns flowOf(Result.failure(IllegalStateException(Arb.string().next())))
                val viewModel = CalendarHomeDDayViewModel(getCalendarDDayUseCase = useCase)

                viewModel.fetch(YearMonth(year = 2026, month = Month.JULY))

                viewModel.uiState.test {
                    awaitItem() shouldBe CalendarHomeDDayUiState()
                    advanceUntilIdle()
                    expectNoEvents()
                }
            }
        }

        test("같은 달을 다시 지정해도 디데이를 다시 조회하지 않는다") {
            runTest(mainDispatcher) {
                val useCase = getCalendarDDayUseCase(JULY_FETCH_RANGE to Result.success(listOf(Arb.calendarDDay().next())))
                val viewModel = CalendarHomeDDayViewModel(getCalendarDDayUseCase = useCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe CalendarHomeDDayUiState()
                    viewModel.fetch(YearMonth(year = 2026, month = Month.JULY))
                    awaitItem()
                    viewModel.fetch(YearMonth(year = 2026, month = Month.JULY))
                    advanceUntilIdle()
                    expectNoEvents()
                }

                verify(exactly = 1) { useCase(parameter = any()) }
            }
        }

        test("달을 지정하기 전에는 디데이를 조회하지 않는다") {
            runTest(mainDispatcher) {
                val useCase = getCalendarDDayUseCase()
                val viewModel = CalendarHomeDDayViewModel(getCalendarDDayUseCase = useCase)

                viewModel.uiState.test {
                    awaitItem() shouldBe CalendarHomeDDayUiState()
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

    private companion object {
        val JULY_FETCH_RANGE: LocalDateRange =
            LocalDate(year = 2026, month = Month.MAY, day = 1)..LocalDate(year = 2026, month = Month.SEPTEMBER, day = 30)
        val SEPTEMBER_FETCH_RANGE: LocalDateRange =
            LocalDate(year = 2026, month = Month.JULY, day = 1)..LocalDate(year = 2026, month = Month.NOVEMBER, day = 30)
    }
}
