package io.github.taetae98coding.diary.feature.timetable.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.calendar.CalendarDefaults
import io.github.taetae98coding.diary.compose.calendar.CalendarItemDefaults
import io.github.taetae98coding.diary.compose.calendar.search.CalendarSearchOpener
import io.github.taetae98coding.diary.compose.calendar.search.rememberCalendarSearchOpener
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.shortcut.keyShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.timetable.Timetable
import io.github.taetae98coding.diary.compose.timetable.TimetableEvent
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import io.github.taetae98coding.diary.feature.timetable.api.CalendarTimetableNavKey
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@Composable
internal fun CalendarTimetableScaffold(
    onEvent: (CalendarTimetableScaffoldEvent) -> Unit,
    onTimetableEvent: (TimetableEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: CalendarTimetableScaffoldState = rememberCalendarTimetableScaffoldState(),
    memoUiStateProvider: () -> CalendarTimetableMemoUiState = { CalendarTimetableMemoUiState() },
    holidayUiStateProvider: () -> CalendarTimetableHolidayUiState = { CalendarTimetableHolidayUiState() },
    weatherUiStateProvider: () -> CalendarTimetableWeatherUiState = { CalendarTimetableWeatherUiState() },
    birthdayUiStateProvider: () -> CalendarTimetableBirthdayUiState = { CalendarTimetableBirthdayUiState() },
) {
    val coroutineScope = rememberCoroutineScope()
    val searchOpener = rememberCalendarSearchOpener(snackbarHostState = state.snackbarHostState)

    Scaffold(
        modifier =
            modifier.keyShortcut { keyEvent ->
                when {
                    keyEvent.isPreviousShortcut() -> {
                        coroutineScope.launch { state.timetableState.animateScrollToPrevious() }
                        true
                    }

                    keyEvent.isNextShortcut() -> {
                        coroutineScope.launch { state.timetableState.animateScrollToNext() }
                        true
                    }

                    else -> false
                }
            },
        topBar = {
            CalendarTimetableTopBar(
                onEvent = onEvent,
                state = state,
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.snackbarHostState) },
    ) { paddingValues ->
        CalendarTimetableContent(
            onEvent = onEvent,
            onTimetableEvent = onTimetableEvent,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            state = state,
            searchOpener = searchOpener,
            memoUiStateProvider = memoUiStateProvider,
            holidayUiStateProvider = holidayUiStateProvider,
            weatherUiStateProvider = weatherUiStateProvider,
            birthdayUiStateProvider = birthdayUiStateProvider,
        )
    }
}

@Composable
private fun CalendarTimetableContent(
    onEvent: (CalendarTimetableScaffoldEvent) -> Unit,
    onTimetableEvent: (TimetableEvent) -> Unit,
    searchOpener: CalendarSearchOpener,
    modifier: Modifier = Modifier,
    state: CalendarTimetableScaffoldState = rememberCalendarTimetableScaffoldState(),
    memoUiStateProvider: () -> CalendarTimetableMemoUiState = { CalendarTimetableMemoUiState() },
    holidayUiStateProvider: () -> CalendarTimetableHolidayUiState = { CalendarTimetableHolidayUiState() },
    weatherUiStateProvider: () -> CalendarTimetableWeatherUiState = { CalendarTimetableWeatherUiState() },
    birthdayUiStateProvider: () -> CalendarTimetableBirthdayUiState = { CalendarTimetableBirthdayUiState() },
) {
    val calendarColors = CalendarDefaults.colors()
    val nonHolidayNameColor = CalendarItemDefaults.nonHolidayNameColor()
    val birthdayColor = CalendarItemDefaults.birthdayColor()

    Timetable(
        modifier = modifier,
        state = state.timetableState,
        onEvent = onTimetableEvent,
        nowProvider = { state.now },
        holidayProvider = {
            holidayUiStateProvider()
                .holidayList
                .filter { it.isHoliday }
                .map { it.dateRange }
        },
        colors = calendarColors,
    ) {
        calendarTimetableItems(
            memoProvider = { memoUiStateProvider().memoList },
            birthdayProvider = { birthdayUiStateProvider().birthdayList },
            holidayProvider = { holidayUiStateProvider().holidayList },
            birthdayColor = birthdayColor,
            holidayNameColor = calendarColors.sundayAndHolidayColor,
            nonHolidayNameColor = nonHolidayNameColor,
            onEvent = onEvent,
            onHolidayClick = { holiday -> searchOpener.openHolidaySearch(name = holiday.name) },
        )
        calendarTimetableWeatherItems(
            weatherProvider = { weatherUiStateProvider().weatherReport.weatherList },
            onWeatherClick = { searchOpener.openWeatherSearch(locationName = weatherUiStateProvider().weatherReport.locationName) },
        )
    }
}

private fun KeyEvent.isPreviousShortcut(): Boolean = type == KeyEventType.KeyDown && key == Key.DirectionLeft

private fun KeyEvent.isNextShortcut(): Boolean = type == KeyEventType.KeyDown && key == Key.DirectionRight

private class CalendarTimetableTypePreviewParameter : PreviewParameterProvider<CalendarTimetableNavKey.Type> {
    override val values: Sequence<CalendarTimetableNavKey.Type> = CalendarTimetableNavKey.Type.entries.asSequence()
}

@ScreenPreview
@Composable
private fun CalendarTimetableScaffoldPreview(
    @PreviewParameter(CalendarTimetableTypePreviewParameter::class) type: CalendarTimetableNavKey.Type,
) {
    val memoUiState = remember { CalendarTimetableMemoUiState(memoList = listOf(previewCalendarMemo(), previewCalendarTimedMemo())) }
    val holidayUiState = remember { CalendarTimetableHolidayUiState(holidayList = listOf(previewHoliday())) }
    val weatherUiState = remember { CalendarTimetableWeatherUiState(weatherReport = CalendarWeatherReport(weatherList = listOf(previewCalendarWeather()))) }

    DiaryTheme {
        CalendarTimetableScaffold(
            onEvent = {},
            onTimetableEvent = {},
            state = rememberCalendarTimetableScaffoldState(type = type, initialDate = LocalDate(year = 2026, month = 7, day = 19)),
            memoUiStateProvider = { memoUiState },
            holidayUiStateProvider = { holidayUiState },
            weatherUiStateProvider = { weatherUiState },
        )
    }
}
