package io.github.taetae98coding.diary.compose.memo

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

@Composable
public fun UpdateMemoListTodayEffect(state: MemoListState = rememberMemoListState()) {
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        state.updateToday()
    }
}
