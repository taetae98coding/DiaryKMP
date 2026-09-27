@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.calendar.ui.timetable

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.domain.holiday.usecase.FetchHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.GetCalendarHolidayUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
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

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class CalendarTimetableHolidayViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-CALENDAR-TIMETABLE-DOMAIN-013 한 연도의 공휴일 조회가 실패해도 다른 연도의 공휴일은 표시한다") {
            runTest(mainDispatcher) {
                val newYear = fixtureMonkey.giveMeOne<Holiday>()
                val getUseCase = mockk<GetCalendarHolidayUseCase>()
                every { getUseCase(parameter = 2026) } returns flowOf(Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>())))
                every { getUseCase(parameter = 2027) } returns flowOf(Result.success(listOf(newYear)))
                val viewModel = CalendarTimetableHolidayViewModel(fetchHolidayUseCase = successfulFetchHolidayUseCase(), getCalendarHolidayUseCase = getUseCase)

                viewModel.holidayList.test {
                    awaitItem() shouldBe emptyList()

                    viewModel.fetch(dateRange = day(year = 2026, month = 12, day = 31))

                    awaitItem() shouldBe listOf(newYear)
                }
            }
        }

        test("TC-CALENDAR-TIMETABLE-DATA-004 시간표에 들어오거나 날짜가 바뀌면 대상 연도의 공휴일을 동기화한다") {
            runTest(mainDispatcher) {
                val fetchUseCase = successfulFetchHolidayUseCase()
                val viewModel = CalendarTimetableHolidayViewModel(fetchHolidayUseCase = fetchUseCase, getCalendarHolidayUseCase = emptyGetCalendarHolidayUseCase())

                viewModel.fetch(dateRange = day(year = 2026, month = 12, day = 30))
                advanceUntilIdle()
                coVerify(exactly = 1) { fetchUseCase(parameter = 2026) }
                coVerify(exactly = 0) { fetchUseCase(parameter = 2027) }

                viewModel.fetch(dateRange = day(year = 2026, month = 12, day = 31))
                advanceUntilIdle()
                coVerify(exactly = 2) { fetchUseCase(parameter = 2026) }
                coVerify(exactly = 1) { fetchUseCase(parameter = 2027) }
            }
        }

        test("TC-CALENDAR-TIMETABLE-DATA-005 동기화가 진행 중이면 새 동기화를 시작하지 않지만 표시 대상은 바뀐다") {
            runTest(mainDispatcher) {
                val holiday2027 = fixtureMonkey.giveMeOne<Holiday>()
                val pending = CompletableDeferred<Result<List<Holiday>>>()
                val fetchUseCase = mockk<FetchHolidayUseCase>()
                coEvery { fetchUseCase(parameter = any()) } coAnswers { pending.await() }
                val getUseCase = mockk<GetCalendarHolidayUseCase>()
                every { getUseCase(parameter = 2026) } returns flowOf(Result.success(emptyList()))
                every { getUseCase(parameter = 2027) } returns flowOf(Result.success(listOf(holiday2027)))
                val viewModel = CalendarTimetableHolidayViewModel(fetchHolidayUseCase = fetchUseCase, getCalendarHolidayUseCase = getUseCase)

                viewModel.holidayList.test {
                    awaitItem() shouldBe emptyList()
                    viewModel.fetch(dateRange = day(year = 2026, month = 7, day = 15))
                    advanceUntilIdle()

                    viewModel.fetch(dateRange = day(year = 2027, month = 7, day = 15))
                    advanceUntilIdle()

                    expectMostRecentItem() shouldBe listOf(holiday2027)
                    coVerify(exactly = 0) { fetchUseCase(parameter = 2027) }
                    pending.complete(Result.success(emptyList()))
                }
            }
        }

        test("진행 중이던 동기화가 끝난 뒤 날짜가 바뀌면 다시 동기화한다") {
            runTest(mainDispatcher) {
                val fetchUseCase = successfulFetchHolidayUseCase()
                val viewModel = CalendarTimetableHolidayViewModel(fetchHolidayUseCase = fetchUseCase, getCalendarHolidayUseCase = emptyGetCalendarHolidayUseCase())

                viewModel.fetch(dateRange = day(year = 2026, month = 7, day = 15))
                advanceUntilIdle()
                viewModel.fetch(dateRange = day(year = 2027, month = 7, day = 15))
                advanceUntilIdle()

                coVerify(exactly = 1) { fetchUseCase(parameter = 2027) }
            }
        }
    }
}

private fun successfulFetchHolidayUseCase(): FetchHolidayUseCase =
    mockk<FetchHolidayUseCase>().also { useCase ->
        coEvery { useCase(parameter = any()) } returns Result.success(emptyList())
    }

private fun emptyGetCalendarHolidayUseCase(): GetCalendarHolidayUseCase =
    mockk<GetCalendarHolidayUseCase>().also { useCase ->
        every { useCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
    }

private fun day(
    year: Int,
    month: Int,
    day: Int,
): LocalDateRange = LocalDate(year = year, month = month, day = day).let { date -> date..date }
