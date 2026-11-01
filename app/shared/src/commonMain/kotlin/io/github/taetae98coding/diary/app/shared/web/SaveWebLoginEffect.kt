package io.github.taetae98coding.diary.app.shared.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.taetae98coding.diary.app.shared.syncMinActiveState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

// 활성 상태였다가 벗어날 때만 보관한다. 처음부터 활성 상태가 아니면 벗어난 것이 아니다.
@Composable
internal fun SaveWebLoginEffect(save: () -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentSave by rememberUpdatedState(save)

    LaunchedEffect(lifecycle) {
        lifecycle.currentStateFlow
            .map { state -> state.isAtLeast(syncMinActiveState) }
            .distinctUntilChanged()
            .drop(1)
            .filter { isActive -> !isActive }
            .collect { currentSave() }
    }
}
