package io.github.taetae98coding.diary.compose.core.snackbar

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.Res
import io.github.taetae98coding.diary.compose.core.delete_undo_snackbar_undo_action
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
public fun <T> DeleteUndoSnackbarEffect(
    deletedMessage: String,
    deletedId: (T) -> Uuid,
    onRestore: (Uuid) -> Unit,
    effect: Flow<T> = emptyFlow(),
    hostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    UndoSnackbarEffect(
        effect = effect,
        hostState = hostState,
        actionLabel = stringResource(Res.string.delete_undo_snackbar_undo_action),
        message = { deletedMessage },
        onUndo = { value -> onRestore(deletedId(value)) },
    )
}
