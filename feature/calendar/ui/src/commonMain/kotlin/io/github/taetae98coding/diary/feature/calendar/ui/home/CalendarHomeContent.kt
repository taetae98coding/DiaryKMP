package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import io.github.taetae98coding.diary.compose.calendar.Calendar
import io.github.taetae98coding.diary.compose.calendar.CalendarDefaults
import io.github.taetae98coding.diary.compose.calendar.CalendarEvent
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.pulltorefresh.DiaryPullToRefreshBox
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PullToRefreshGestureBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import io.github.taetae98coding.diary.feature.calendar.ui.home.birthday.CalendarHomeBirthdayUiState
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayUiState
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoUiState
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMoveGhost
import io.github.taetae98coding.diary.feature.calendar.ui.home.search.openHolidaySearch
import io.github.taetae98coding.diary.feature.calendar.ui.home.search.openWeatherSearch
import io.github.taetae98coding.diary.feature.calendar.ui.home.weather.CalendarHomeWeatherUiState
import io.github.taetae98coding.diary.feature.calendar.ui.previewCalendarContactBirthday
import io.github.taetae98coding.diary.feature.calendar.ui.previewCalendarMemo
import io.github.taetae98coding.diary.feature.calendar.ui.previewCalendarWeather

@Composable
internal fun CalendarHomeContent(
    onEvent: (CalendarHomeScaffoldEvent) -> Unit,
    onCalendarEvent: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: CalendarHomeScaffoldState = rememberCalendarHomeScaffoldState(),
    weatherUiStateProvider: () -> CalendarHomeWeatherUiState = { CalendarHomeWeatherUiState() },
    holidayUiStateProvider: () -> CalendarHomeHolidayUiState = { CalendarHomeHolidayUiState() },
    memoUiStateProvider: () -> CalendarHomeMemoUiState = { CalendarHomeMemoUiState() },
    birthdayUiStateProvider: () -> CalendarHomeBirthdayUiState = { CalendarHomeBirthdayUiState() },
    uiStateProvider: () -> CalendarHomeScaffoldUiState = { CalendarHomeScaffoldUiState() },
) {
    val calendarColors = CalendarDefaults.colors()
    val nonHolidayNameColor = CalendarHomeDefaults.nonHolidayNameColor()
    val birthdayColor = CalendarHomeDefaults.birthdayColor()
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
                    onEvent = onCalendarEvent,
                    isSelectEnabled = true,
                    isDateClickEnabled = true,
                    isWeekClickEnabled = true,
                    holidayProvider = {
                        holidayUiStateProvider()
                            .holidayList
                            .filter { it.isHoliday }
                            .map { it.dateRange }
                    },
                    primaryDateProvider = { listOfNotNull(state.today) },
                    colors = calendarColors,
                ) {
                    calendarHomeItems(
                        moveState = state.calendarState.moveState,
                        weatherProvider = { weatherUiStateProvider().weatherReport.weatherList },
                        holidayProvider = { holidayUiStateProvider().holidayList },
                        holidayNameColor = calendarColors.sundayAndHolidayColor,
                        nonHolidayNameColor = nonHolidayNameColor,
                        memoProvider = { memoUiStateProvider().memoList },
                        birthdayProvider = { birthdayUiStateProvider().birthdayList },
                        birthdayColor = birthdayColor,
                        onWeatherClick = { uriHandler.openWeatherSearch(locationName = weatherUiStateProvider().weatherReport.locationName) },
                        onHolidayClick = { holiday -> uriHandler.openHolidaySearch(name = holiday.name) },
                        onEvent = onEvent,
                    )
                }
                CalendarHomeMoveGhost(
                    moveState = state.calendarState.moveState,
                    memoProvider = { memoUiStateProvider().memoList },
                )
            }
        }
    }
}

@ScreenPreview
@Composable
private fun CalendarHomeContentPreview() {
    val weatherUiState = remember { CalendarHomeWeatherUiState(weatherReport = CalendarWeatherReport(weatherList = listOf(previewCalendarWeather()))) }
    val memoUiState = remember { CalendarHomeMemoUiState(memoList = listOf(previewCalendarMemo())) }
    val birthdayUiState = remember { CalendarHomeBirthdayUiState(birthdayList = listOf(previewCalendarContactBirthday())) }

    DiaryTheme {
        CalendarHomeContent(
            onEvent = {},
            onCalendarEvent = {},
            modifier = Modifier.fillMaxSize(),
            weatherUiStateProvider = { weatherUiState },
            memoUiStateProvider = { memoUiState },
            birthdayUiStateProvider = { birthdayUiState },
        )
    }
}
