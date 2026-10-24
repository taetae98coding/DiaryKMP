package io.github.taetae98coding.diary.feature.checklist.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.snackbar.UndoSnackbarEffect
import io.github.taetae98coding.diary.feature.checklist.ui.Res
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_home_deleted_message
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_home_undo_action
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun DeleteUndoSnackbarEffect(
    onRestore: (Uuid) -> Unit,
    effect: Flow<ChecklistHomeEffect>,
    snackbarHostState: SnackbarHostState,
) {
    UndoSnackbarEffect(
        actionLabel = stringResource(Res.string.checklist_home_undo_action),
        message = stringResource(Res.string.checklist_home_deleted_message),
        onUndo = { value ->
            when (value) {
                is ChecklistHomeEffect.Deleted -> onRestore(value.id)
            }
        },
        effect = effect,
        snackbarHostState = snackbarHostState,
    )
}
