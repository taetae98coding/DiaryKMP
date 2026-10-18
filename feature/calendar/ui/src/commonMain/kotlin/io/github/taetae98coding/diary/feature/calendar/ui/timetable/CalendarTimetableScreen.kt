package io.github.taetae98coding.diary.feature.calendar.ui.timetable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.timetable.TimetableEvent
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import kotlin.uuid.Uuid

@Composable
internal fun CalendarTimetableScreen(
    navigateUp: () -> Unit,
    navigateToMemoDetail: (Uuid) -> Unit,
    navigateToMemoAdd: (MemoDateTime) -> Unit,
    state: CalendarTimetableScaffoldState,
    memoViewModel: CalendarTimetableViewModel,
    holidayViewModel: CalendarTimetableHolidayViewModel,
    modifier: Modifier = Modifier,
) {
    val memoUiState by memoViewModel.uiState.collectAsStateWithLifecycle()
    val holidayUiState by holidayViewModel.uiState.collectAsStateWithLifecycle()

    UpdateNowEffect(state = state)
    SelectMemoEffect(
        state = state,
        memoViewModel = memoViewModel,
    )
    FetchHolidayEffect(
        state = state,
        holidayViewModel = holidayViewModel,
    )
    CalendarTimetableScaffold(
        onEvent = { event ->
            when (event) {
                is CalendarTimetableScaffoldEvent.ClickNavigateUp -> navigateUp()
                is CalendarTimetableScaffoldEvent.ClickMemo -> navigateToMemoDetail(event.id)
            }
        },
        onTimetableEvent = { event ->
            when (event) {
                is TimetableEvent.SelectTime -> navigateToMemoAdd(MemoDateTime.DateTime(start = event.start, endInclusive = event.endInclusive))
                is TimetableEvent.SelectDate -> navigateToMemoAdd(MemoDateTime.AllDay(dateRange = event.dateRange))
            }
        },
        modifier = modifier,
        state = state,
        memoUiStateProvider = { memoUiState },
        holidayUiStateProvider = { holidayUiState },
    )
}
