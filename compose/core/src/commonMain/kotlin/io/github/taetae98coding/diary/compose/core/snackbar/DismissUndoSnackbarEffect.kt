package io.github.taetae98coding.diary.compose.core.snackbar

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.drop

@Composable
public fun DismissUndoSnackbarEffect(
    keyProvider: () -> Any?,
    hostState: SnackbarHostState,
) {
    LaunchedEffect(hostState) {
        snapshotFlow(keyProvider)
            .drop(1)
            .collect {
                hostState.currentSnackbarData
                    ?.takeIf { data -> data.visuals.actionLabel != null }
                    ?.dismiss()
            }
    }
}
