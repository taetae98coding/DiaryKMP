package io.github.taetae98coding.diary.feature.search.ui.home.memo

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.snackbar.UndoSnackbarEffect
import io.github.taetae98coding.diary.feature.search.ui.Res
import io.github.taetae98coding.diary.feature.search.ui.search_home_memo_deleted_message
import io.github.taetae98coding.diary.feature.search.ui.search_home_memo_finished_message
import io.github.taetae98coding.diary.feature.search.ui.search_home_memo_restarted_message
import io.github.taetae98coding.diary.feature.search.ui.search_home_undo_action
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SearchHomeMemoUndoSnackbarEffect(
    onUndo: (SearchHomeMemoEffect) -> Unit,
    effect: Flow<SearchHomeMemoEffect> = emptyFlow(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val finishedMessage = stringResource(Res.string.search_home_memo_finished_message)
    val restartedMessage = stringResource(Res.string.search_home_memo_restarted_message)
    val deletedMessage = stringResource(Res.string.search_home_memo_deleted_message)

    UndoSnackbarEffect(
        effect = effect,
        hostState = snackbarHostState,
        actionLabel = stringResource(Res.string.search_home_undo_action),
        message = { value ->
            when (value) {
                is SearchHomeMemoEffect.Finished -> finishedMessage
                is SearchHomeMemoEffect.Restarted -> restartedMessage
                is SearchHomeMemoEffect.Deleted -> deletedMessage
            }
        },
        onUndo = onUndo,
    )
}
