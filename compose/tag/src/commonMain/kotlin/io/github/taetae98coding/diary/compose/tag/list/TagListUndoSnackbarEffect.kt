package io.github.taetae98coding.diary.compose.tag.list

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.snackbar.UndoSnackbarEffect
import io.github.taetae98coding.diary.compose.tag.Res
import io.github.taetae98coding.diary.compose.tag.tag_list_deleted_message
import io.github.taetae98coding.diary.compose.tag.tag_list_finished_message
import io.github.taetae98coding.diary.compose.tag.tag_list_restarted_message
import io.github.taetae98coding.diary.compose.tag.tag_list_undo_action
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource

@Composable
public fun TagListUndoSnackbarEffect(
    onUndo: (TagListEffect) -> Unit,
    effect: Flow<TagListEffect> = emptyFlow(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val finishedMessage = stringResource(Res.string.tag_list_finished_message)
    val restartedMessage = stringResource(Res.string.tag_list_restarted_message)
    val deletedMessage = stringResource(Res.string.tag_list_deleted_message)

    UndoSnackbarEffect(
        effect = effect,
        hostState = snackbarHostState,
        actionLabel = stringResource(Res.string.tag_list_undo_action),
        message = { value ->
            when (value) {
                is TagListEffect.Finished -> finishedMessage
                is TagListEffect.Restarted -> restartedMessage
                is TagListEffect.Deleted -> deletedMessage
            }
        },
        onUndo = onUndo,
    )
}
