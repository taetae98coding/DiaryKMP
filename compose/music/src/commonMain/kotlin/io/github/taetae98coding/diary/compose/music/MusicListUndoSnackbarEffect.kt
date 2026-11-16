package io.github.taetae98coding.diary.compose.music

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.snackbar.DeleteUndoSnackbarEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
public fun MusicListUndoSnackbarEffect(
    onRestore: (Uuid) -> Unit,
    effect: Flow<MusicListEffect> = emptyFlow(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    DeleteUndoSnackbarEffect(
        deletedMessage = stringResource(Res.string.music_list_deleted_message),
        deletedId = { value ->
            when (value) {
                is MusicListEffect.Deleted -> value.id
            }
        },
        onRestore = onRestore,
        effect = effect,
        hostState = snackbarHostState,
    )
}
