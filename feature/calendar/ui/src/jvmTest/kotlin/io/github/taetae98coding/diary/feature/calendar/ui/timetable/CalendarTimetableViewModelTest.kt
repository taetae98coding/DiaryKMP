@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.calendar.ui.timetable

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.testing.memo.calendarMemo
import io.github.taetae98coding.diary.domain.memo.usecase.GetCalendarMemoUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class CalendarTimetableViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-CALENDAR-TIMETABLE-DATA-001 날짜를 옮기면 옮긴 날짜의 조회 기간으로 메모를 다시 조회한다") {
            runTest(mainDispatcher) {
                val firstRange = date(day = 22)..date(day = 24)
                val secondRange = date(day = 23)..date(day = 25)
                val firstList = listOf(memo())
                val secondList = listOf(memo(), memo())
                val useCase = getCalendarMemoUseCase(firstRange to Result.success(firstList), secondRange to Result.success(secondList))
                val viewModel = CalendarTimetableViewModel(getCalendarMemoUseCase = useCase)

                viewModel.memoList.test {
                    awaitItem() shouldBe emptyList()

                    viewModel.fetch(dateRange = (date(day = 23)..date(day = 23)).calendarTimetableFetchDateRange())
                    awaitItem() shouldBe firstList

                    viewModel.fetch(dateRange = (date(day = 24)..date(day = 24)).calendarTimetableFetchDateRange())
                    awaitItem() shouldBe secondList
                }

                verify(exactly = 1) { useCase(parameter = firstRange) }
                verify(exactly = 1) { useCase(parameter = secondRange) }
            }
        }

        test("TC-CALENDAR-TIMETABLE-DOMAIN-011 메모 조회에 실패하면 메모 없이 표시한다") {
            runTest(mainDispatcher) {
                val dateRange = date(day = 22)..date(day = 24)
                val useCase = getCalendarMemoUseCase(dateRange to Result.failure(IllegalStateException()))
                val viewModel = CalendarTimetableViewModel(getCalendarMemoUseCase = useCase)

                viewModel.memoList.test {
                    awaitItem() shouldBe emptyList()
                    viewModel.fetch(dateRange = dateRange)
                    mainDispatcher.scheduler.advanceUntilIdle()
                    expectNoEvents()
                }

                verify(exactly = 1) { useCase(parameter = dateRange) }
            }
        }

        test("같은 조회 기간을 다시 요청해도 다시 조회하지 않는다") {
            runTest(mainDispatcher) {
                val dateRange = date(day = 22)..date(day = 24)
                val memoList = listOf(memo())
                val useCase = getCalendarMemoUseCase(dateRange to Result.success(memoList))
                val viewModel = CalendarTimetableViewModel(getCalendarMemoUseCase = useCase)

                viewModel.memoList.test {
                    awaitItem() shouldBe emptyList()
                    viewModel.fetch(dateRange = dateRange)
                    awaitItem() shouldBe memoList
                    viewModel.fetch(dateRange = dateRange)
                    mainDispatcher.scheduler.advanceUntilIdle()
                    expectNoEvents()
                }

                verify(exactly = 1) { useCase(parameter = dateRange) }
            }
        }
    }
}

private fun date(day: Int): LocalDate = LocalDate(year = 2026, month = 9, day = day)

private fun getCalendarMemoUseCase(vararg resultByDateRange: Pair<LocalDateRange, Result<List<CalendarMemo>>>): GetCalendarMemoUseCase {
    val results = resultByDateRange.toMap()

    return mockk<GetCalendarMemoUseCase>().also { useCase ->
        every { useCase(parameter = any()) } answers {
            flowOf(results[firstArg<LocalDateRange>()] ?: Result.success(emptyList()))
        }
    }
}

private fun memo(): CalendarMemo {
    val date = date(day = 22 + (fixtureMonkey.giveMeOne<Int>().toUInt() % 3u).toInt())

    return fixtureMonkey.calendarMemo(dateTime = MemoDateTime.AllDay(dateRange = date..date))
}
