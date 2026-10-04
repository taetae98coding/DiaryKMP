package io.github.taetae98coding.diary.feature.calendar.ui.timetable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.minutes

@Composable
internal fun UpdateNowEffect(state: CalendarTimetableScaffoldState) {
    // 시각이 흐르는 동안 STARTED 상태에서만 1분마다 현재 시각을 갱신해야 하므로 Flow 수집이 아닌 repeatOnLifecycle 타이머를 쓴다.
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
internal fun SelectMemoEffect(
    state: CalendarTimetableScaffoldState,
    memoViewModel: CalendarTimetableViewModel,
) {
    LaunchedEffect(state, memoViewModel) {
        snapshotFlow { state.timetableState.currentDateRange }
            .collect { dateRange -> memoViewModel.select(dateRange = dateRange.calendarTimetableFetchDateRange()) }
    }
}

@Composable
internal fun FetchHolidayEffect(
    state: CalendarTimetableScaffoldState,
    holidayViewModel: CalendarTimetableHolidayViewModel,
) {
    LaunchedEffect(state, holidayViewModel) {
        snapshotFlow { state.timetableState.currentDateRange }
            .collect { dateRange -> holidayViewModel.fetch(dateRange = dateRange) }
    }
}

private val NowUpdateInterval = 1.minutes
