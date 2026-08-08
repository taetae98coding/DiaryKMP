package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import io.github.taetae98coding.diary.compose.core.dialog.DiaryDatePickerDialogHost
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.shortcut.keyShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import kotlinx.coroutines.launch
import kotlinx.datetime.yearMonth

@Composable
internal fun CalendarHomeScaffold(
    onEvent: (CalendarHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: CalendarHomeScaffoldState = rememberCalendarHomeScaffoldState(),
    weatherProvider: () -> CalendarWeatherReport = { CalendarWeatherReport() },
    holidayProvider: () -> List<Holiday> = { emptyList() },
    memoProvider: () -> List<CalendarMemo> = { emptyList() },
    birthdayProvider: () -> List<CalendarContactBirthday> = { emptyList() },
    filterUiStateProvider: () -> CalendarHomeScaffoldFilterUiState = { CalendarHomeScaffoldFilterUiState() },
    uiStateProvider: () -> CalendarHomeScaffoldUiState = { CalendarHomeScaffoldUiState() },
) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier =
            modifier.keyShortcut(isEnableProvider = { !state.isDatePickerVisible }) { keyEvent ->
                when {
                    keyEvent.isPreviousMonthShortcut() -> {
                        coroutineScope.launch { state.calendarState.animateScrollToPreviousMonth() }
                        true
                    }

                    keyEvent.isNextMonthShortcut() -> {
                        coroutineScope.launch { state.calendarState.animateScrollToNextMonth() }
                        true
                    }

                    else -> false
                }
            },
        topBar = {
            CalendarHomeTopBar(
                state = state,
                filterUiStateProvider = filterUiStateProvider,
                onEvent = onEvent,
            )
        },
    ) { paddingValues ->
        CalendarHomeContent(
            state = state,
            weatherProvider = weatherProvider,
            holidayProvider = holidayProvider,
            memoProvider = memoProvider,
            birthdayProvider = birthdayProvider,
            uiStateProvider = uiStateProvider,
            onEvent = onEvent,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        )
    }

    CalendarHomeDialogHost(state = state)
}

@Composable
private fun CalendarHomeDialogHost(state: CalendarHomeScaffoldState = rememberCalendarHomeScaffoldState()) {
    val coroutineScope = rememberCoroutineScope()

    DiaryDatePickerDialogHost(
        initialDateProvider = { state.datePickerInitialDate },
        onConfirm = { date ->
            coroutineScope.launch { state.calendarState.animateScrollTo(date.yearMonth) }
        },
        dialogState = state.datePickerDialogState,
    )
}

private fun KeyEvent.isPreviousMonthShortcut(): Boolean = type == KeyEventType.KeyDown && key == Key.DirectionLeft

private fun KeyEvent.isNextMonthShortcut(): Boolean = type == KeyEventType.KeyDown && key == Key.DirectionRight

@ScreenPreview
@Composable
private fun CalendarHomeScaffoldPreview() {
    DiaryTheme {
        CalendarHomeScaffold(onEvent = {})
    }
}
