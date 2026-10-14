package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.taetae98coding.diary.compose.calendar.CalendarState
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.calendar.select.CalendarSelectState
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.yearMonth
import kotlin.time.Clock

@Stable
internal class CalendarHomeScaffoldState(
    val calendarState: CalendarState,
    val datePickerDialogState: DialogState,
    val snackbarHostState: SnackbarHostState,
) {
    val calendarSelectState: CalendarSelectState
        get() = calendarState.selectState

    var today: LocalDate? by mutableStateOf(null)
        private set

    val isDatePickerVisible: Boolean
        get() = datePickerDialogState.isVisible

    val datePickerInitialDate: LocalDate
        get() = calendarState.currentYearMonth.firstDay

    fun updateToday() {
        today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    }

    suspend fun animateScrollToToday() {
        val today = today ?: return

        calendarState.animateScrollTo(today.yearMonth)
    }

    fun showDatePicker() {
        datePickerDialogState.show()
    }
}

@Composable
internal fun rememberCalendarHomeScaffoldState(
    calendarState: CalendarState = rememberCalendarState(),
    datePickerDialogState: DialogState = rememberDialogState(),
): CalendarHomeScaffoldState {
    val snackbarHostState = remember { SnackbarHostState() }

    return remember(calendarState, datePickerDialogState, snackbarHostState) {
        CalendarHomeScaffoldState(
            calendarState = calendarState,
            datePickerDialogState = datePickerDialogState,
            snackbarHostState = snackbarHostState,
        )
    }
}
