package io.github.taetae98coding.diary.feature.core.memo.finished

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
import io.github.taetae98coding.diary.feature.core.Res
import io.github.taetae98coding.diary.feature.core.entity_memo_finished_list_deleted_message
import io.github.taetae98coding.diary.feature.core.entity_memo_finished_list_restarted_message
import io.github.taetae98coding.diary.feature.core.entity_memo_finished_list_undo_action
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
public fun EntityMemoFinishedListScreen(
    navigateUp: () -> Unit,
    navigateToMemoDetail: (Uuid) -> Unit,
    emptyTitle: String,
    memoViewModel: EntityMemoFinishedListViewModel,
    syncViewModel: SyncRefreshViewModel,
    modifier: Modifier = Modifier,
) {
    val memoListState = rememberMemoListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by memoViewModel.uiState.collectAsStateWithLifecycle()
    val memoPagingItems = memoViewModel.memoPagingData.collectAsLazyPagingItems()
    val syncUiState by syncViewModel.uiState.collectAsStateWithLifecycle()
    val sortUiState by memoViewModel.sortUiState.collectAsStateWithLifecycle()
    val sortSheetState = rememberDialogState()

    UpdateMemoListTodayEffect(state = memoListState)
    EntityMemoFinishedListUndoSnackbarEffect(
        onFinish = memoViewModel::finish,
        onRestore = memoViewModel::restore,
        effect = memoViewModel.effect,
        snackbarHostState = snackbarHostState,
    )

    EntityMemoFinishedListScaffold(
        onEvent = { event ->
            when (event) {
                is EntityMemoFinishedListScaffoldEvent.ClickNavigateUp -> navigateUp()
                is EntityMemoFinishedListScaffoldEvent.ClickSort -> sortSheetState.show()
                is EntityMemoFinishedListScaffoldEvent.SelectSort -> memoViewModel.select(sort = event.sort)
            }
        },
        onMemoListEvent = { event ->
            when (event) {
                is MemoListEvent.ClickMemo -> navigateToMemoDetail(event.id)
                is MemoListEvent.SwipeFinish -> memoViewModel.finish(id = event.id)
                is MemoListEvent.SwipeRestart -> memoViewModel.restart(id = event.id)
                is MemoListEvent.SwipeDelete -> memoViewModel.delete(id = event.id)
                is MemoListEvent.Refresh -> syncViewModel.refresh()
            }
        },
        emptyTitle = emptyTitle,
        modifier = modifier,
        memoListState = memoListState,
        sortSheetState = sortSheetState,
        snackbarHostState = snackbarHostState,
        memoPagingItems = memoPagingItems,
        syncUiStateProvider = { syncUiState },
        uiStateProvider = { uiState },
        sortProvider = { sortUiState.sort },
    )
}

@Composable
private fun EntityMemoFinishedListUndoSnackbarEffect(
    onFinish: (Uuid) -> Unit,
    onRestore: (Uuid) -> Unit,
    effect: Flow<EntityMemoFinishedListEffect> = emptyFlow(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val restartedMessage = stringResource(Res.string.entity_memo_finished_list_restarted_message)
    val deletedMessage = stringResource(Res.string.entity_memo_finished_list_deleted_message)

    UndoSnackbarEffect(
        effect = effect,
        hostState = snackbarHostState,
        actionLabel = stringResource(Res.string.entity_memo_finished_list_undo_action),
        message = { value ->
            when (value) {
                is EntityMemoFinishedListEffect.Restarted -> restartedMessage
                is EntityMemoFinishedListEffect.Deleted -> deletedMessage
            }
        },
        onUndo = { value ->
            when (value) {
                is EntityMemoFinishedListEffect.Restarted -> onFinish(value.id)
                is EntityMemoFinishedListEffect.Deleted -> onRestore(value.id)
            }
        },
    )
}
