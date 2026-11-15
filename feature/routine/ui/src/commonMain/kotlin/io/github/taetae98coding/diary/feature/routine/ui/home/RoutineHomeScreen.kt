package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.snackbar.DismissUndoSnackbarEffect
import io.github.taetae98coding.diary.compose.routine.RoutineListEvent
import io.github.taetae98coding.diary.compose.routine.RoutineListUndoSnackbarEffect
import io.github.taetae98coding.diary.compose.routine.UpdateRoutineListTodayEffect
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarViewModel
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday.RoutineHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.routine.ui.home.record.RoutineHomeRecordUndoSnackbarEffect
import io.github.taetae98coding.diary.feature.routine.ui.home.viewmode.RoutineHomeViewMode
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Composable
internal fun RoutineHomeScreen(
    navigateToAdd: () -> Unit,
    navigateToDetail: (Uuid) -> Unit,
    navigateToFilter: () -> Unit,
    navigateToFinishedList: () -> Unit,
    componentVisibleProvider: () -> RoutineHomeScaffoldComponentVisible,
    homeReselectEvent: Flow<Unit>,
    routineViewModel: RoutineHomeViewModel,
    calendarViewModel: RoutineHomeCalendarViewModel,
    holidayViewModel: RoutineHomeHolidayViewModel,
    syncViewModel: SyncRefreshViewModel,
    modifier: Modifier = Modifier,
) {
    val state = rememberRoutineHomeScaffoldState()
    val routinePagingItems = routineViewModel.routinePagingData.collectAsLazyPagingItems()
    val filterUiState by routineViewModel.filterUiState.collectAsStateWithLifecycle()
    val calendarUiState by calendarViewModel.uiState.collectAsStateWithLifecycle()
    val holidayUiState by holidayViewModel.uiState.collectAsStateWithLifecycle()
    val syncUiState by syncViewModel.uiState.collectAsStateWithLifecycle()

    RoutineHomeScreenEffect(
        homeReselectEvent = homeReselectEvent,
        state = state,
        routineViewModel = routineViewModel,
        calendarViewModel = calendarViewModel,
        holidayViewModel = holidayViewModel,
    )

    RoutineHomeScaffold(
        onEvent = { event ->
            when (event) {
                is RoutineHomeScaffoldEvent.ClickAdd -> navigateToAdd()

                is RoutineHomeScaffoldEvent.ClickFilter -> navigateToFilter()

                is RoutineHomeScaffoldEvent.ClickFinishedList -> navigateToFinishedList()

                is RoutineHomeScaffoldEvent.Refresh -> {
                    syncViewModel.refresh()
                    if (state.viewMode == RoutineHomeViewMode.CALENDAR) {
                        holidayViewModel.fetch(yearMonth = state.calendarState.currentYearMonth)
                    }
                }

                is RoutineHomeScaffoldEvent.QuickRecord -> routineViewModel.quickRecord(record = event.record, today = state.today)

                is RoutineHomeScaffoldEvent.AmountRecord -> routineViewModel.amountRecord(record = event.record, today = state.today)

                is RoutineHomeScaffoldEvent.ClickCalendarRecord -> navigateToDetail(event.routineId)
            }
        },
        onRoutineListEvent = { event ->
            when (event) {
                is RoutineListEvent.ClickRoutine -> navigateToDetail(event.id)
                is RoutineListEvent.SwipeFinish -> routineViewModel.finish(id = event.id)
                is RoutineListEvent.SwipeRestart -> routineViewModel.restart(id = event.id)
                is RoutineListEvent.SwipeDelete -> routineViewModel.delete(id = event.id)
            }
        },
        modifier = modifier,
        state = state,
        routinePagingItems = routinePagingItems,
        filterUiStateProvider = { filterUiState },
        calendarUiStateProvider = { calendarUiState },
        holidayUiStateProvider = { holidayUiState },
        syncUiStateProvider = { syncUiState },
        componentVisibleProvider = componentVisibleProvider,
    )
}

@Composable
private fun RoutineHomeScreenEffect(
    homeReselectEvent: Flow<Unit>,
    state: RoutineHomeScaffoldState,
    routineViewModel: RoutineHomeViewModel,
    calendarViewModel: RoutineHomeCalendarViewModel,
    holidayViewModel: RoutineHomeHolidayViewModel,
) {
    UpdateRoutineListTodayEffect(state = state.listState)
    UpdateRoutineHomeTodayEffect(
        routineViewModel = routineViewModel,
        state = state,
    )
    FetchCalendarRecordEffect(
        calendarViewModel = calendarViewModel,
        state = state,
    )
    FetchHolidayEffect(
        holidayViewModel = holidayViewModel,
        state = state,
    )
    ScrollToStartOnReselectEffect(
        reselectEvent = homeReselectEvent,
        state = state,
    )
    RoutineListUndoSnackbarEffect(
        onUndo = routineViewModel::undo,
        effect = routineViewModel.effect,
        snackbarHostState = state.snackbarHostState,
    )
    RoutineHomeRecordUndoSnackbarEffect(
        onUndo = routineViewModel::undoRecord,
        effect = routineViewModel.recordEffect,
        snackbarHostState = state.snackbarHostState,
    )
    DismissUndoSnackbarEffect(
        keyProvider = { state.viewMode },
        hostState = state.snackbarHostState,
    )
}
