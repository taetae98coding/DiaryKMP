package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarHomeScreenHolidayNameTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-018 표시 중인 달에 걸친 공휴일의 이름이 캘린더에 표시된다`() {
        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            holidayList = listOf(holiday(name = CONSTITUTION_DAY_NAME, isHoliday = true, date = july(day = 17))),
        )

        composeRule.onNodeWithText(CONSTITUTION_DAY_NAME).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-019 공휴일 여부가 거짓인 항목의 이름도 캘린더에 표시된다`() {
        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            holidayList =
                listOf(
                    holiday(name = CONSTITUTION_DAY_NAME, isHoliday = true, date = july(day = 17)),
                    holiday(name = MIDSUMMER_DAY_NAME, isHoliday = false, date = july(day = 20)),
                ),
        )

        composeRule.onNodeWithText(CONSTITUTION_DAY_NAME).assertIsDisplayed()
        composeRule.onNodeWithText(MIDSUMMER_DAY_NAME).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-020 표시 중인 달과 겹치지 않는 공휴일의 이름은 표시되지 않는다`() {
        setCalendarHomeScreen(
            initialYearMonth = JULY_2026,
            holidayList =
                listOf(
                    holiday(
                        name = NEW_YEAR_DAY_NAME,
                        isHoliday = true,
                        date = LocalDate(year = 2026, month = Month.JANUARY, day = 1),
                    ),
                ),
        )

        composeRule.onNodeWithText(NEW_YEAR_DAY_NAME).assertDoesNotExist()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-021 주 경계를 넘는 공휴일의 이름은 겹치는 각 주에 표시된다`() {
        setCalendarHomeScreen(
            initialYearMonth = FEBRUARY_2026,
            holidayList =
                listOf(
                    Holiday(
                        name = LONG_HOLIDAY_NAME,
                        isHoliday = true,
                        dateRange = february(day = 13)..february(day = 15),
                    ),
                ),
        )

        composeRule
            .onAllNodesWithText(LONG_HOLIDAY_NAME)
            .fetchSemanticsNodes()
            .size shouldBe 2
    }

    private fun setCalendarHomeScreen(
        initialYearMonth: YearMonth,
        holidayList: List<Holiday>,
    ) {
        val holidayViewModel =
            mockk<CalendarHomeHolidayViewModel>().also { viewModel ->
                every { viewModel.fetch(any()) } returns Unit
                every { viewModel.holidayList } returns MutableStateFlow(holidayList)
                every { viewModel.isFetching } returns MutableStateFlow(false)
            }
        val memoViewModel =
            mockk<CalendarHomeMemoViewModel>().also { viewModel ->
                every { viewModel.fetch(any()) } returns Unit
                every { viewModel.memoList } returns MutableStateFlow(emptyList())
                every { viewModel.filterUiState } returns MutableStateFlow(CalendarHomeScaffoldFilterUiState())
            }

        composeRule.setContent {
            val state =
                rememberCalendarHomeScaffoldState(
                    calendarState = rememberCalendarState(initialYearMonth = initialYearMonth),
                )

            DiaryTheme {
                CalendarHomeScreen(
                    navigateToMemoDetail = {},
                    navigateToMemoAdd = {},
                    navigateToContactDetail = {},
                    birthdayViewModel = birthdayViewModel(),
                    navigateToFilter = {},
                    navigateToTimetable = {},
                    state = state,
                    holidayViewModel = holidayViewModel,
                    memoViewModel = memoViewModel,
                    weatherViewModel = weatherViewModel(),
                    syncViewModel = syncViewModel(),
                    permissionManager = rememberPermissionManager(),
                )
            }
        }
    }

    private fun holiday(
        name: String,
        isHoliday: Boolean,
        date: LocalDate,
    ): Holiday =
        Holiday(
            name = name,
            isHoliday = isHoliday,
            dateRange = date..date,
        )

    private fun july(day: Int): LocalDate = LocalDate(year = 2026, month = Month.JULY, day = day)

    private fun february(day: Int): LocalDate = LocalDate(year = 2026, month = Month.FEBRUARY, day = day)

    companion object {
        private const val CONSTITUTION_DAY_NAME = "제헌절"
        private const val MIDSUMMER_DAY_NAME = "초복"
        private const val NEW_YEAR_DAY_NAME = "새해"
        private const val LONG_HOLIDAY_NAME = "연휴"
        private val JULY_2026 = YearMonth(year = 2026, month = Month.JULY)
        private val FEBRUARY_2026 = YearMonth(year = 2026, month = Month.FEBRUARY)
    }
}
