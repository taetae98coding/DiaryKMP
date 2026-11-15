package io.github.taetae98coding.diary.feature.search.ui.home.routine

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import io.github.taetae98coding.diary.compose.routine.RoutineListState

@Composable
internal fun UpdateSearchHomeRoutineTodayEffect(
    routineViewModel: SearchHomeRoutineViewModel,
    listState: RoutineListState,
) {
    LaunchedEffect(listState, routineViewModel) {
        snapshotFlow { listState.today }
            .collect { today -> routineViewModel.updateToday(today = today) }
    }
}
