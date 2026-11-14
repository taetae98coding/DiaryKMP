package io.github.taetae98coding.diary.compose.routine

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

@Composable
public fun UpdateRoutineListTodayEffect(state: RoutineListState = rememberRoutineListState()) {
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        state.updateToday()
    }
}
