package io.github.taetae98coding.diary.feature.core.routine

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import io.github.taetae98coding.diary.compose.routine.RoutineListState
import kotlinx.datetime.LocalDate

@Composable
public fun UpdateRoutineTodayEffect(
    onUpdate: (LocalDate) -> Unit,
    listState: RoutineListState,
) {
    val currentOnUpdate by rememberUpdatedState(onUpdate)

    LaunchedEffect(listState) {
        snapshotFlow { listState.today }
            .collect { today -> currentOnUpdate(today) }
    }
}
