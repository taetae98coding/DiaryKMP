package io.github.taetae98coding.diary.compose.place

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.snackbar.UndoSnackbarEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
public fun PlaceListUndoSnackbarEffect(
    onRestore: (Uuid) -> Unit,
    effect: Flow<PlaceListEffect> = emptyFlow(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val deletedMessage = stringResource(Res.string.place_list_deleted_message)

    UndoSnackbarEffect(
        effect = effect,
        hostState = snackbarHostState,
        actionLabel = stringResource(Res.string.place_list_undo_action),
        message = { value ->
            when (value) {
                is PlaceListEffect.Deleted -> deletedMessage
            }
        },
        onUndo = { value ->
            when (value) {
                is PlaceListEffect.Deleted -> onRestore(value.id)
            }
        },
    )
}
