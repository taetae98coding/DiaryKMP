package io.github.taetae98coding.diary.feature.routine.ui.home.calendar

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import io.github.taetae98coding.diary.compose.calendar.Calendar
import io.github.taetae98coding.diary.compose.calendar.CalendarDefaults
import io.github.taetae98coding.diary.compose.calendar.CalendarItemDefaults
import io.github.taetae98coding.diary.compose.calendar.search.rememberCalendarSearchOpener
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.pulltorefresh.DiaryPullToRefreshBox
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PullToRefreshGestureBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.routine.ui.home.RoutineHomeScaffoldEvent
import io.github.taetae98coding.diary.feature.routine.ui.home.RoutineHomeScaffoldState
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday.RoutineHomeHolidayUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday.holidayItem
import io.github.taetae98coding.diary.feature.routine.ui.home.rememberRoutineHomeScaffoldState
import io.github.taetae98coding.diary.feature.routine.ui.previewCalendarRoutineRecord
import kotlinx.coroutines.launch

@Composable
internal fun RoutineHomeCalendar(
    onEvent: (RoutineHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: RoutineHomeScaffoldState = rememberRoutineHomeScaffoldState(),
    calendarUiStateProvider: () -> RoutineHomeCalendarUiState = { RoutineHomeCalendarUiState() },
    holidayUiStateProvider: () -> RoutineHomeHolidayUiState = { RoutineHomeHolidayUiState() },
    isRefreshingProvider: () -> Boolean = { false },
    bottomPadding: Dp = DiaryTheme.dimens.screenVerticalPadding,
) {
    val calendarColors = CalendarDefaults.colors()
    val nonHolidayNameColor = CalendarItemDefaults.nonHolidayNameColor()
    val coroutineScope = rememberCoroutineScope()
    val searchOpener = rememberCalendarSearchOpener(snackbarHostState = state.snackbarHostState)

    DiaryPullToRefreshBox(
        isRefreshingProvider = isRefreshingProvider,
        onRefresh = { onEvent(RoutineHomeScaffoldEvent.Refresh) },
        modifier = modifier,
    ) {
        PullToRefreshGestureBox(modifier = Modifier.fillMaxSize()) {
            Calendar(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(bottom = bottomPadding),
                state = state.calendarState,
                holidayProvider = {
                    holidayUiStateProvider()
                        .holidayList
                        .filter { holiday -> holiday.isHoliday }
                        .map { holiday -> holiday.dateRange }
                },
                primaryDateProvider = { listOf(state.today) },
                colors = calendarColors,
            ) {
                group {
                    routineRecordItem(
                        recordProvider = { calendarUiStateProvider().recordList },
                        onRecordClick = { routineId -> onEvent(RoutineHomeScaffoldEvent.ClickCalendarRecord(routineId = routineId)) },
                    )
                    holidayItem(
                        holidayProvider = { holidayUiStateProvider().holidayList },
                        holidayNameColor = calendarColors.sundayAndHolidayColor,
                        nonHolidayNameColor = nonHolidayNameColor,
                        onHolidayClick = { holiday -> coroutineScope.launch { searchOpener.openHolidaySearch(name = holiday.name) } },
                    )
                }
            }
        }
    }
}

@ScreenPreview
@Composable
private fun RoutineHomeCalendarPreview() {
    val calendarUiState = remember { RoutineHomeCalendarUiState(recordList = listOf(previewCalendarRoutineRecord())) }

    DiaryTheme {
        RoutineHomeCalendar(
            onEvent = {},
            modifier = Modifier.fillMaxSize(),
            calendarUiStateProvider = { calendarUiState },
        )
    }
}
