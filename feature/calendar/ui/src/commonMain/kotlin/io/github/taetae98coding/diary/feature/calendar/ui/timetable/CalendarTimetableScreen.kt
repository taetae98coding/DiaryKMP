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
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

@Composable
internal fun CalendarTimetableScreen(
    navigateUp: () -> Unit,
    navigateToMemoDetail: (Uuid) -> Unit,
    state: CalendarTimetableScaffoldState,
    viewModel: CalendarTimetableViewModel,
    modifier: Modifier = Modifier,
) {
    val memoList by viewModel.memoList.collectAsStateWithLifecycle()

    UpdateNowEffect(state = state)
    FetchMemoEffect(
        state = state,
        viewModel = viewModel,
    )
    CalendarTimetableScaffold(
        onEvent = { event ->
            when (event) {
                is CalendarTimetableScaffoldEvent.ClickNavigateUp -> navigateUp()
                is CalendarTimetableScaffoldEvent.ClickMemo -> navigateToMemoDetail(event.id)
            }
        },
        modifier = modifier,
        state = state,
        memoProvider = { memoList },
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
    viewModel: CalendarTimetableViewModel,
) {
    LaunchedEffect(state, viewModel) {
        snapshotFlow { state.timetableState.currentDateRange }
            .collect { dateRange -> viewModel.fetch(dateRange = dateRange.calendarTimetableFetchDateRange()) }
    }
}

private val NowUpdateInterval = 1.minutes
