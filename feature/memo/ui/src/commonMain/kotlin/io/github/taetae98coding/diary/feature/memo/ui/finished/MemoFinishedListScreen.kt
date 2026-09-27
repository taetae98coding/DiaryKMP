package io.github.taetae98coding.diary.feature.memo.ui.finished

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
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_finished_list_deleted_message
import io.github.taetae98coding.diary.feature.memo.ui.memo_finished_list_restarted_message
import io.github.taetae98coding.diary.feature.memo.ui.memo_finished_list_undo_action
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.Uuid

@Composable
internal fun MemoFinishedListScreen(
    navigateUp: () -> Unit,
    navigateToDetail: (Uuid) -> Unit,
    memoViewModel: MemoFinishedListViewModel,
    syncViewModel: MemoFinishedListSyncViewModel,
    modifier: Modifier = Modifier,
) {
    val memoListState = rememberMemoListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val memoPagingItems = memoViewModel.memoPagingData.collectAsLazyPagingItems()
    val memoListUiState by syncViewModel.uiState.collectAsStateWithLifecycle()
    val sort by memoViewModel.sort.collectAsStateWithLifecycle()
    val sortSheetState = rememberDialogState()

    UpdateMemoListTodayEffect(state = memoListState)
    MemoFinishedListUndoSnackbarEffect(
        effect = memoViewModel.effect,
        snackbarHostState = snackbarHostState,
        onFinish = memoViewModel::finish,
        onRestore = memoViewModel::restore,
    )

    MemoFinishedListScaffold(
        memoListState = memoListState,
        sortSheetState = sortSheetState,
        snackbarHostState = snackbarHostState,
        memoPagingItems = memoPagingItems,
        memoListUiStateProvider = { memoListUiState },
        sortProvider = { sort },
        onEvent = { event ->
            when (event) {
                is MemoFinishedListScaffoldEvent.ClickNavigateUp -> navigateUp()
                is MemoFinishedListScaffoldEvent.ClickSort -> sortSheetState.show()
                is MemoFinishedListScaffoldEvent.SelectSort -> memoViewModel.select(sort = event.sort)
            }
        },
        onMemoListEvent = { event ->
            when (event) {
                is MemoListEvent.ClickMemo -> navigateToDetail(event.id)
                is MemoListEvent.SwipeFinish -> memoViewModel.finish(id = event.id)
                is MemoListEvent.SwipeRestart -> memoViewModel.restart(id = event.id)
                is MemoListEvent.SwipeDelete -> memoViewModel.delete(id = event.id)
                is MemoListEvent.Refresh -> syncViewModel.refresh()
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun MemoFinishedListUndoSnackbarEffect(
    onFinish: (Uuid) -> Unit,
    onRestore: (Uuid) -> Unit,
    effect: Flow<MemoFinishedListEffect> = emptyFlow(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val restartedMessage = stringResource(Res.string.memo_finished_list_restarted_message)
    val deletedMessage = stringResource(Res.string.memo_finished_list_deleted_message)

    UndoSnackbarEffect(
        effect = effect,
        hostState = snackbarHostState,
        actionLabel = stringResource(Res.string.memo_finished_list_undo_action),
        message = { value ->
            when (value) {
                is MemoFinishedListEffect.Restarted -> restartedMessage
                is MemoFinishedListEffect.Deleted -> deletedMessage
            }
        },
        onUndo = { value ->
            when (value) {
                is MemoFinishedListEffect.Restarted -> onFinish(value.id)
                is MemoFinishedListEffect.Deleted -> onRestore(value.id)
            }
        },
    )
}
