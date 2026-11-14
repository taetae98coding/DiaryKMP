package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarViewModel
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday.RoutineHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.routine.ui.home.viewmode.RoutineHomeViewMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull

@Composable
internal fun ScrollToStartOnReselectEffect(
    reselectEvent: Flow<Unit> = emptyFlow(),
    state: RoutineHomeScaffoldState = rememberRoutineHomeScaffoldState(),
) {
    CollectEffect(effect = reselectEvent) {
        state.animateScrollToStart()
    }
}

@Composable
internal fun UpdateRoutineHomeTodayEffect(
    routineViewModel: RoutineHomeViewModel,
    state: RoutineHomeScaffoldState,
) {
    LaunchedEffect(state, routineViewModel) {
        snapshotFlow { state.today }
            .collect { today -> routineViewModel.updateToday(today = today) }
    }
}

@Composable
internal fun FetchCalendarRecordEffect(
    calendarViewModel: RoutineHomeCalendarViewModel,
    state: RoutineHomeScaffoldState,
) {
    LaunchedEffect(state, calendarViewModel) {
        snapshotFlow { state.calendarState.currentYearMonth.takeIf { state.viewMode == RoutineHomeViewMode.CALENDAR } }
            .filterNotNull()
            .collect { yearMonth -> calendarViewModel.fetch(yearMonth = yearMonth) }
    }
}

@Composable
internal fun FetchHolidayEffect(
    holidayViewModel: RoutineHomeHolidayViewModel,
    state: RoutineHomeScaffoldState,
) {
    LaunchedEffect(state, holidayViewModel) {
        snapshotFlow { state.calendarState.currentYearMonth.takeIf { state.viewMode == RoutineHomeViewMode.CALENDAR } }
            .collect { yearMonth -> yearMonth?.let { value -> holidayViewModel.fetch(yearMonth = value) } }
    }
}
