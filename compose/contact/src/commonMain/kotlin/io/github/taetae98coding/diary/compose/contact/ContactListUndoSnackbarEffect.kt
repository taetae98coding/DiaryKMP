package io.github.taetae98coding.diary.compose.contact

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.snackbar.UndoSnackbarEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
public fun ContactListUndoSnackbarEffect(
    onRestore: (Uuid) -> Unit,
    effect: Flow<ContactListEffect> = emptyFlow(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val deletedMessage = stringResource(Res.string.contact_list_deleted_message)

    UndoSnackbarEffect(
        effect = effect,
        hostState = snackbarHostState,
        actionLabel = stringResource(Res.string.contact_list_undo_action),
        message = { value ->
            when (value) {
                is ContactListEffect.Deleted -> deletedMessage
            }
        },
        onUndo = { value ->
            when (value) {
                is ContactListEffect.Deleted -> onRestore(value.id)
            }
        },
    )
}
