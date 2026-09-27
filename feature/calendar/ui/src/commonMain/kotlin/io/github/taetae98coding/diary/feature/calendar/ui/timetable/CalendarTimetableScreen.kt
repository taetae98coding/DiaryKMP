package io.github.taetae98coding.diary.feature.calendar.ui.timetable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import io.github.taetae98coding.diary.compose.timetable.TimetableEvent
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.minutes
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
    val memoList by memoViewModel.memoList.collectAsStateWithLifecycle()
    val holidayList by holidayViewModel.holidayList.collectAsStateWithLifecycle()

    UpdateNowEffect(state = state)
    FetchMemoEffect(
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
        memoProvider = { memoList },
        holidayProvider = { holidayList },
    )
}

@Composable
private fun UpdateNowEffect(state: CalendarTimetableScaffoldState) {
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(state, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                state.updateNow()
                delay(NowUpdateInterval)
            }
        }
    }
}

@Composable
private fun FetchMemoEffect(
    state: CalendarTimetableScaffoldState,
    memoViewModel: CalendarTimetableViewModel,
) {
    LaunchedEffect(state, memoViewModel) {
        snapshotFlow { state.timetableState.currentDateRange }
            .collect { dateRange -> memoViewModel.fetch(dateRange = dateRange.calendarTimetableFetchDateRange()) }
    }
}

@Composable
private fun FetchHolidayEffect(
    state: CalendarTimetableScaffoldState,
    holidayViewModel: CalendarTimetableHolidayViewModel,
) {
    LaunchedEffect(state, holidayViewModel) {
        snapshotFlow { state.timetableState.currentDateRange }
            .collect { dateRange -> holidayViewModel.fetch(dateRange = dateRange) }
    }
}

private val NowUpdateInterval = 1.minutes
