package io.github.taetae98coding.diary.compose.routine

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.snackbar.UndoSnackbarEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource

@Composable
public fun RoutineListUndoSnackbarEffect(
    onUndo: (RoutineListEffect) -> Unit,
    effect: Flow<RoutineListEffect> = emptyFlow(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val finishedMessage = stringResource(Res.string.routine_list_finished_message)
    val restartedMessage = stringResource(Res.string.routine_list_restarted_message)
    val deletedMessage = stringResource(Res.string.routine_list_deleted_message)

    UndoSnackbarEffect(
        effect = effect,
        hostState = snackbarHostState,
        actionLabel = stringResource(Res.string.routine_list_undo_action),
        message = { value ->
            when (value) {
                is RoutineListEffect.Finished -> finishedMessage
                is RoutineListEffect.Restarted -> restartedMessage
                is RoutineListEffect.Deleted -> deletedMessage
            }
        },
        onUndo = onUndo,
    )
}
