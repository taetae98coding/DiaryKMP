package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import io.github.taetae98coding.diary.compose.calendar.Calendar
import io.github.taetae98coding.diary.compose.calendar.CalendarDefault
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.pulltorefresh.DiaryPullToRefreshBox
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PullToRefreshGestureBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import io.github.taetae98coding.diary.feature.calendar.ui.previewCalendarContactBirthday
import io.github.taetae98coding.diary.feature.calendar.ui.previewCalendarMemo
import io.github.taetae98coding.diary.feature.calendar.ui.previewCalendarWeather

@Composable
internal fun CalendarHomeContent(
    onEvent: (CalendarHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: CalendarHomeScaffoldState = rememberCalendarHomeScaffoldState(),
    weatherProvider: () -> CalendarWeatherReport = { CalendarWeatherReport() },
    holidayProvider: () -> List<Holiday> = { emptyList() },
    memoProvider: () -> List<CalendarMemo> = { emptyList() },
    birthdayProvider: () -> List<CalendarContactBirthday> = { emptyList() },
    uiStateProvider: () -> CalendarHomeScaffoldUiState = { CalendarHomeScaffoldUiState() },
) {
    val calendarColors = CalendarDefault.colors()
    val nonHolidayNameColor = CalendarHomeDefault.nonHolidayNameColor()
    val birthdayColor = CalendarHomeDefault.birthdayColor()
    val uriHandler = LocalUriHandler.current

    DiaryPullToRefreshBox(
        isRefreshingProvider = { uiStateProvider().isRefreshing },
        onRefresh = { onEvent(CalendarHomeScaffoldEvent.Refresh) },
        modifier = modifier,
    ) {
        PullToRefreshGestureBox(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize()) {
                Calendar(
                    modifier = Modifier.fillMaxSize(),
                    state = state.calendarState,
                    onSelect = { dateRange -> onEvent(CalendarHomeScaffoldEvent.SelectDate(dateRange = dateRange)) },
                    holidayProvider = {
                        holidayProvider()
                            .filter { it.isHoliday }
                            .map { it.dateRange }
                    },
                    primaryDateProvider = { listOfNotNull(state.today) },
                    colors = calendarColors,
                ) {
                    calendarHomeItems(
                        moveState = state.calendarState.moveState,
                        weatherProvider = { weatherProvider().weatherList },
                        holidayProvider = holidayProvider,
                        holidayNameColor = calendarColors.sundayAndHolidayColor,
                        nonHolidayNameColor = nonHolidayNameColor,
                        memoProvider = memoProvider,
                        birthdayProvider = birthdayProvider,
                        birthdayColor = birthdayColor,
                        onWeatherClick = { uriHandler.openWeatherSearch(locationName = weatherProvider().locationName) },
                        onHolidayClick = { holiday -> uriHandler.openHolidaySearch(name = holiday.name) },
                        onEvent = onEvent,
                    )
                }
                CalendarHomeMoveGhost(
                    moveState = state.calendarState.moveState,
                    memoProvider = memoProvider,
                )
            }
        }
    }
}

@ScreenPreview
@Composable
private fun CalendarHomeContentPreview() {
    val weatherReport = remember { CalendarWeatherReport(weatherList = listOf(previewCalendarWeather())) }
    val memoList = remember { listOf(previewCalendarMemo()) }
    val birthdayList = remember { listOf(previewCalendarContactBirthday()) }

    DiaryTheme {
        CalendarHomeContent(
            onEvent = {},
            modifier = Modifier.fillMaxSize(),
            weatherProvider = { weatherReport },
            memoProvider = { memoList },
            birthdayProvider = { birthdayList },
        )
    }
}
