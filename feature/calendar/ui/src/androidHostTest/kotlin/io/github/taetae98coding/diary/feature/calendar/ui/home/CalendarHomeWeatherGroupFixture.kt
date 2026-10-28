package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.weather.CalendarWeather
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherTemperature
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayUiState
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoUiState
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlin.uuid.Uuid

internal object CalendarHomeWeatherGroupFixture {
    const val SUNDAY_TEMPERATURE_TEXT = "17.8°/28.2°"
    const val TRIP_TITLE = "여행"
    const val APPOINTMENT_TITLE = "약속"
    const val HOLIDAY_NAME = "임시공휴일"
    const val BIRTHDAY_NAME = "홍길동"
    const val BIRTHDAY_TEXT = "🎂 홍길동"

    val JULY_2026: YearMonth = YearMonth(year = 2026, month = Month.JULY)

    private const val MEMO_COLOR = 0xFF0000FF

    /** 7월 12일(일)의 날씨. 같은 주의 월요일 날씨는 [mondayWeather]다. */
    fun sundayWeather(): CalendarWeather =
        weather(
            date = july(day = 12),
            min = 17.8,
            max = 28.2,
            description = "맑음",
        )

    fun mondayWeather(): CalendarWeather =
        weather(
            date = july(day = 13),
            min = 15.1,
            max = 25.4,
            description = "흐림",
        )

    fun memo(
        title: String,
        start: LocalDate,
        endInclusive: LocalDate,
    ): CalendarMemo =
        CalendarMemo(
            id = Uuid.random(),
            title = title,
            color = MEMO_COLOR,
            dateTime = MemoDateTime.AllDay(dateRange = start..endInclusive),
        )

    fun holiday(dateRange: LocalDateRange = july(day = 18)..july(day = 18)): Holiday =
        Holiday(
            name = HOLIDAY_NAME,
            isHoliday = true,
            dateRange = dateRange,
        )

    fun birthday(date: LocalDate): CalendarContactBirthday =
        CalendarContactBirthday(
            contactId = Uuid.random(),
            name = BIRTHDAY_NAME,
            date = date,
        )

    fun july(day: Int): LocalDate = LocalDate(year = 2026, month = Month.JULY, day = day)

    private fun weather(
        date: LocalDate,
        min: Double,
        max: Double,
        description: String,
    ): CalendarWeather =
        calendarWeather(
            date = date,
            temperature = CalendarWeatherTemperature.MinMax(min = min, max = max),
            descriptionList = listOf(description),
        )
}

internal fun ComposeContentTestRule.setCalendarHomeWeatherGroupScreen(
    weatherList: List<CalendarWeather>,
    memoList: List<CalendarMemo> = emptyList(),
    holidayList: List<Holiday> = emptyList(),
    birthdayList: List<CalendarContactBirthday> = emptyList(),
) {
    val holidayViewModel =
        mockk<CalendarHomeHolidayViewModel>().also { viewModel ->
            every { viewModel.fetch(any()) } returns Unit
            every { viewModel.uiState } returns MutableStateFlow(CalendarHomeHolidayUiState(holidayList = holidayList))
        }
    val memoViewModel =
        mockk<CalendarHomeMemoViewModel>().also { viewModel ->
            every { viewModel.select(any()) } returns Unit
            every { viewModel.uiState } returns MutableStateFlow(CalendarHomeMemoUiState(memoList = memoList))
            every { viewModel.filterUiState } returns MutableStateFlow(CalendarHomeScaffoldFilterUiState())
        }

    setContent {
        val state =
            rememberCalendarHomeScaffoldState(
                calendarState = rememberCalendarState(initialYearMonth = CalendarHomeWeatherGroupFixture.JULY_2026),
            )

        DiaryTheme {
            CalendarHomeScreen(
                navigateToMemoDetail = {},
                navigateToMemoAdd = {},
                navigateToContactDetail = {},
                birthdayViewModel = birthdayViewModel(birthdayListFlow = MutableStateFlow(birthdayList)),
                navigateToFilter = {},
                navigateToTimetable = {},
                state = state,
                holidayViewModel = holidayViewModel,
                memoViewModel = memoViewModel,
                weatherViewModel = weatherViewModel(weatherReportFlow = MutableStateFlow(CalendarWeatherReport(weatherList = weatherList))),
                syncViewModel = syncViewModel(),
                permissionManager = rememberPermissionManager(),
            )
        }
    }
}
