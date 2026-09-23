package io.github.taetae98coding.diary.feature.tag.ui.memo.finished

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.snackbar.UndoSnackbarEffect
import io.github.taetae98coding.diary.compose.memo.list.MemoListEvent
import io.github.taetae98coding.diary.compose.memo.list.UpdateMemoListTodayEffect
import io.github.taetae98coding.diary.compose.memo.list.rememberMemoListState
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_memo_finished_list_deleted_message
import io.github.taetae98coding.diary.feature.tag.ui.tag_memo_finished_list_restarted_message
import io.github.taetae98coding.diary.feature.tag.ui.tag_memo_finished_list_undo_action
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun TagMemoFinishedListScreen(
    navigateUp: () -> Unit,
    navigateToMemoDetail: (Uuid) -> Unit,
    memoViewModel: TagMemoFinishedListViewModel,
    syncViewModel: TagMemoFinishedListSyncViewModel,
    modifier: Modifier = Modifier,
) {
    val memoListState = rememberMemoListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by memoViewModel.uiState.collectAsStateWithLifecycle()
    val memoPagingItems = memoViewModel.memoPagingData.collectAsLazyPagingItems()
    val memoListUiState by syncViewModel.uiState.collectAsStateWithLifecycle()
    val sort by memoViewModel.sort.collectAsStateWithLifecycle()
    val sortSheetState = rememberDialogState()

    UpdateMemoListTodayEffect(state = memoListState)
    TagMemoFinishedListUndoSnackbarEffect(
        effect = memoViewModel.effect,
        snackbarHostState = snackbarHostState,
        onFinish = memoViewModel::finish,
        onRestore = memoViewModel::restore,
    )

    TagMemoFinishedListScaffold(
        memoListState = memoListState,
        sortSheetState = sortSheetState,
        snackbarHostState = snackbarHostState,
        memoPagingItems = memoPagingItems,
        onEvent = { event ->
            when (event) {
                is TagMemoFinishedListScaffoldEvent.ClickNavigateUp -> navigateUp()
                is TagMemoFinishedListScaffoldEvent.ClickSort -> sortSheetState.show()
                is TagMemoFinishedListScaffoldEvent.SelectSort -> memoViewModel.select(sort = event.sort)
            }
        },
        onMemoListEvent = { event ->
            when (event) {
                is MemoListEvent.ClickMemo -> navigateToMemoDetail(event.id)
                is MemoListEvent.SwipeFinish -> memoViewModel.restart(id = event.id)
                is MemoListEvent.SwipeDelete -> memoViewModel.delete(id = event.id)
                is MemoListEvent.Refresh -> syncViewModel.refresh()
            }
        },
        modifier = modifier,
        memoListUiStateProvider = { memoListUiState },
        uiStateProvider = { uiState },
        sortProvider = { sort },
    )
}

@Composable
private fun TagMemoFinishedListUndoSnackbarEffect(
    onFinish: (Uuid) -> Unit,
    onRestore: (Uuid) -> Unit,
    effect: Flow<TagMemoFinishedListEffect> = emptyFlow(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val restartedMessage = stringResource(Res.string.tag_memo_finished_list_restarted_message)
    val deletedMessage = stringResource(Res.string.tag_memo_finished_list_deleted_message)

    UndoSnackbarEffect(
        effect = effect,
        hostState = snackbarHostState,
        actionLabel = stringResource(Res.string.tag_memo_finished_list_undo_action),
        message = { value ->
            when (value) {
                is TagMemoFinishedListEffect.Restarted -> restartedMessage
                is TagMemoFinishedListEffect.Deleted -> deletedMessage
            }
        },
        onUndo = { value ->
            when (value) {
                is TagMemoFinishedListEffect.Restarted -> onFinish(value.id)
                is TagMemoFinishedListEffect.Deleted -> onRestore(value.id)
            }
        },
    )
}
