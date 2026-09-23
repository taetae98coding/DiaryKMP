package io.github.taetae98coding.diary.compose.memo.list

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.snackbar.UndoSnackbarEffect
import io.github.taetae98coding.diary.compose.memo.Res
import io.github.taetae98coding.diary.compose.memo.memo_list_deleted_message
import io.github.taetae98coding.diary.compose.memo.memo_list_finished_message
import io.github.taetae98coding.diary.compose.memo.memo_list_undo_action
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
public fun MemoListUndoSnackbarEffect(
    onRestart: (Uuid) -> Unit,
    onRestore: (Uuid) -> Unit,
    effect: Flow<MemoListEffect> = emptyFlow(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val finishedMessage = stringResource(Res.string.memo_list_finished_message)
    val deletedMessage = stringResource(Res.string.memo_list_deleted_message)

    UndoSnackbarEffect(
        effect = effect,
        hostState = snackbarHostState,
        actionLabel = stringResource(Res.string.memo_list_undo_action),
        message = { value ->
            when (value) {
                is MemoListEffect.Finished -> finishedMessage
                is MemoListEffect.Deleted -> deletedMessage
            }
        },
        onUndo = { value ->
            when (value) {
                is MemoListEffect.Finished -> onRestart(value.id)
                is MemoListEffect.Deleted -> onRestore(value.id)
            }
        },
    )
}
