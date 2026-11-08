package io.github.taetae98coding.diary.compose.dday

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

@Composable
public fun UpdateDDayListTodayEffect(state: DDayListState = rememberDDayListState()) {
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        state.updateToday()
    }
}
