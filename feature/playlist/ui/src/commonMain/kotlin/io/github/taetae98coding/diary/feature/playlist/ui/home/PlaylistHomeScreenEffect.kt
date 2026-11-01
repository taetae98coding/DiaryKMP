package io.github.taetae98coding.diary.feature.playlist.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.snackbar.UndoSnackbarEffect
import io.github.taetae98coding.diary.feature.playlist.ui.Res
import io.github.taetae98coding.diary.feature.playlist.ui.playlist_home_deleted_message
import io.github.taetae98coding.diary.feature.playlist.ui.playlist_home_undo_action
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun DeleteUndoSnackbarEffect(
    onRestore: (Uuid) -> Unit,
    effect: Flow<PlaylistHomeEffect>,
    snackbarHostState: SnackbarHostState,
) {
    UndoSnackbarEffect(
        actionLabel = stringResource(Res.string.playlist_home_undo_action),
        message = stringResource(Res.string.playlist_home_deleted_message),
        onUndo = { value ->
            when (value) {
                is PlaylistHomeEffect.Deleted -> onRestore(value.id)
            }
        },
        effect = effect,
        snackbarHostState = snackbarHostState,
    )
}
