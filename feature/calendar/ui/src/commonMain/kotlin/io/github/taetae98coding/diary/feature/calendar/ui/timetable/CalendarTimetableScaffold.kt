package io.github.taetae98coding.diary.feature.calendar.ui.timetable

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.calendar.CalendarDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.shortcut.keyShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.timetable.Timetable
import io.github.taetae98coding.diary.compose.timetable.TimetableEvent
import io.github.taetae98coding.diary.feature.calendar.api.CalendarTimetableNavKey
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeDefaults
import io.github.taetae98coding.diary.feature.calendar.ui.home.search.openHolidaySearch
import io.github.taetae98coding.diary.feature.calendar.ui.previewCalendarMemo
import io.github.taetae98coding.diary.feature.calendar.ui.previewCalendarTimedMemo
import io.github.taetae98coding.diary.feature.calendar.ui.previewHoliday
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
) {
    val coroutineScope = rememberCoroutineScope()
    val calendarColors = CalendarDefaults.colors()
    val nonHolidayNameColor = CalendarHomeDefaults.nonHolidayNameColor()
    val uriHandler = LocalUriHandler.current

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
    ) { paddingValues ->
        Timetable(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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
                holidayProvider = { holidayUiStateProvider().holidayList },
                holidayNameColor = calendarColors.sundayAndHolidayColor,
                nonHolidayNameColor = nonHolidayNameColor,
                onEvent = onEvent,
                onHolidayClick = { holiday -> uriHandler.openHolidaySearch(name = holiday.name) },
            )
        }
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

    DiaryTheme {
        CalendarTimetableScaffold(
            onEvent = {},
            onTimetableEvent = {},
            state = rememberCalendarTimetableScaffoldState(type = type, initialDate = LocalDate(year = 2026, month = 7, day = 19)),
            memoUiStateProvider = { memoUiState },
            holidayUiStateProvider = { holidayUiState },
        )
    }
}
