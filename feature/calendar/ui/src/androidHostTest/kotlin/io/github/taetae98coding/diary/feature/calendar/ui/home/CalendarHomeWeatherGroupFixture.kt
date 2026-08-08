package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.weather.CalendarWeather
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherTemperature
import io.github.taetae98coding.diary.feature.calendar.ui.permission.rememberLocationPermissionRequester
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
    const val MONDAY_TEMPERATURE_TEXT = "15.1°/25.4°"
    const val NEXT_SUNDAY_TEMPERATURE_TEXT = "19.6°/29.3°"
    const val TRIP_TITLE = "여행"
    const val APPOINTMENT_TITLE = "약속"
    const val WORKSHOP_TITLE = "워크숍"
    const val HOLIDAY_NAME = "임시공휴일"

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

    /** 7월 19일(일)의 날씨로, [sundayWeather]와 다른 주에 표시된다. */
    fun nextSundayWeather(): CalendarWeather =
        weather(
            date = july(day = 19),
            min = 19.6,
            max = 29.3,
            description = "비",
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
            every { viewModel.memoList } returns MutableStateFlow(memoList)
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
                birthdayViewModel = birthdayViewModel(),
                navigateToFilter = {},
                state = state,
                holidayViewModel = holidayViewModel,
                memoViewModel = memoViewModel,
                weatherViewModel = weatherViewModel(weatherReportFlow = MutableStateFlow(CalendarWeatherReport(weatherList = weatherList))),
                syncViewModel = syncViewModel(),
                locationPermissionRequester = rememberLocationPermissionRequester(),
            )
        }
    }
}
