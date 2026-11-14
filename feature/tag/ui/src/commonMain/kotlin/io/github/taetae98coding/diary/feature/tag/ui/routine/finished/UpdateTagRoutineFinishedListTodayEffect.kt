package io.github.taetae98coding.diary.feature.tag.ui.routine.finished

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import io.github.taetae98coding.diary.compose.routine.RoutineListState

@Composable
internal fun UpdateTagRoutineFinishedListTodayEffect(
    routineViewModel: TagRoutineFinishedListViewModel,
    listState: RoutineListState,
) {
    LaunchedEffect(listState, routineViewModel) {
        snapshotFlow { listState.today }
            .collect { today -> routineViewModel.updateToday(today = today) }
    }
}
