package io.github.taetae98coding.diary.compose.core.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
public fun <T> CollectEffect(
    effect: Flow<T> = emptyFlow(),
    onEffect: suspend (T) -> Unit,
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val latestOnEffect by rememberUpdatedState(onEffect)

    LaunchedEffect(effect, lifecycle) {
        effect
            .flowWithLifecycle(lifecycle)
            .collect { value -> latestOnEffect(value) }
    }
}
