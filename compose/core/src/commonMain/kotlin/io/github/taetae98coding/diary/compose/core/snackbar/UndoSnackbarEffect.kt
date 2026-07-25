package io.github.taetae98coding.diary.compose.core.snackbar

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

@Composable
public fun <T> UndoSnackbarEffect(
    actionLabel: String,
    message: (T) -> String,
    onUndo: (T) -> Unit,
    effect: Flow<T> = emptyFlow(),
    hostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val coroutineScope = rememberCoroutineScope()

    CollectEffect(effect) { value ->
        coroutineScope.launch {
            val result =
                hostState.showImmediate(
                    message = message(value),
                    actionLabel = actionLabel,
                )

            if (result == SnackbarResult.ActionPerformed) {
                onUndo(value)
            }
        }
    }
}
