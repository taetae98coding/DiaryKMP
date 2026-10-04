package io.github.taetae98coding.diary.feature.qr.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.snackbar.UndoSnackbarEffect
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.qr_home_deleted_message
import io.github.taetae98coding.diary.feature.qr.ui.qr_home_undo_action
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun DeleteUndoSnackbarEffect(
    onRestore: (Uuid) -> Unit,
    effect: Flow<QrHomeEffect>,
    snackbarHostState: SnackbarHostState,
) {
    UndoSnackbarEffect(
        actionLabel = stringResource(Res.string.qr_home_undo_action),
        message = stringResource(Res.string.qr_home_deleted_message),
        onUndo = { value ->
            when (value) {
                is QrHomeEffect.Deleted -> onRestore(value.id)
            }
        },
        effect = effect,
        snackbarHostState = snackbarHostState,
    )
}
