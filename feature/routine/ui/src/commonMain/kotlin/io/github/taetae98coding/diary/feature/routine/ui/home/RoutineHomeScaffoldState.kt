package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.calendar.CalendarState
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.routine.RoutineListState
import io.github.taetae98coding.diary.compose.routine.rememberRoutineListState
import io.github.taetae98coding.diary.feature.routine.ui.home.amount.RoutineAmountDialogState
import io.github.taetae98coding.diary.feature.routine.ui.home.amount.rememberRoutineAmountDialogState
import io.github.taetae98coding.diary.feature.routine.ui.home.viewmode.RoutineHomeViewMode
import io.github.taetae98coding.diary.feature.routine.ui.home.viewmode.RoutineHomeViewModeState
import io.github.taetae98coding.diary.feature.routine.ui.home.viewmode.rememberRoutineHomeViewModeState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.yearMonth

@Stable
internal class RoutineHomeScaffoldState(
    val viewModeState: RoutineHomeViewModeState,
    val listState: RoutineListState,
    val gridState: LazyGridState,
    val calendarState: CalendarState,
    val datePickerDialogState: DialogState,
    val amountDialogState: RoutineAmountDialogState,
    val snackbarHostState: SnackbarHostState,
) {
    val viewMode: RoutineHomeViewMode
        get() = viewModeState.viewMode

    val today: LocalDate
        get() = listState.today

    val isDialogVisible: Boolean
        get() = datePickerDialogState.isVisible || amountDialogState.isVisible

    val datePickerInitialDate: LocalDate
        get() = calendarState.currentYearMonth.firstDay

    fun toggleViewMode() {
        viewModeState.toggle()
        if (viewMode == RoutineHomeViewMode.LIST) {
            gridState.requestScrollToItem(index = 0)
        }
    }

    suspend fun animateScrollToToday() {
        calendarState.animateScrollTo(today.yearMonth)
    }

    suspend fun animateScrollToStart() {
        when (viewMode) {
            RoutineHomeViewMode.LIST -> gridState.animateScrollToItem(index = 0)
            RoutineHomeViewMode.CALENDAR -> animateScrollToToday()
        }
    }
}

@Composable
internal fun rememberRoutineHomeScaffoldState(
    viewModeState: RoutineHomeViewModeState = rememberRoutineHomeViewModeState(),
    listState: RoutineListState = rememberRoutineListState(),
    calendarState: CalendarState = rememberCalendarState(),
): RoutineHomeScaffoldState {
    val gridState = rememberLazyGridState()
    val datePickerDialogState = rememberDialogState()
    val amountDialogState = rememberRoutineAmountDialogState()
    val snackbarHostState = remember { SnackbarHostState() }

    return remember(viewModeState, listState, gridState, calendarState, datePickerDialogState, amountDialogState, snackbarHostState) {
        RoutineHomeScaffoldState(
            viewModeState = viewModeState,
            listState = listState,
            gridState = gridState,
            calendarState = calendarState,
            datePickerDialogState = datePickerDialogState,
            amountDialogState = amountDialogState,
            snackbarHostState = snackbarHostState,
        )
    }
}
