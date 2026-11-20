package io.github.taetae98coding.diary.compose.web

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.snackbar.DeleteUndoSnackbarEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
public fun WebListUndoSnackbarEffect(
    onRestore: (Uuid) -> Unit,
    effect: Flow<WebListEffect> = emptyFlow(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    DeleteUndoSnackbarEffect(
        deletedMessage = stringResource(Res.string.web_list_deleted_message),
        deletedId = { value ->
            when (value) {
                is WebListEffect.Deleted -> value.id
            }
        },
        onRestore = onRestore,
        effect = effect,
        hostState = snackbarHostState,
    )
}
