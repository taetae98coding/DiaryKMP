package io.github.taetae98coding.diary.feature.core.memo

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.memo.list.MemoListEffect
import io.github.taetae98coding.diary.compose.memo.list.MemoListEvent
import io.github.taetae98coding.diary.compose.memo.list.MemoListUndoSnackbarEffect
import io.github.taetae98coding.diary.compose.memo.list.UpdateMemoListTodayEffect
import io.github.taetae98coding.diary.compose.memo.list.rememberMemoListState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import kotlin.uuid.Uuid

@Composable
public fun EntityDetailMemoContent(
    memoViewModel: MemoListViewModel<MemoListEffect>,
    syncViewModel: SyncRefreshViewModel,
    navigateToMemoDetail: (Uuid) -> Unit,
    emptyTitle: String,
    emptyDescription: String,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    filterProvider: () -> Any? = { Unit },
    trailing: @Composable (() -> Unit)? = null,
) {
    val syncUiState by syncViewModel.uiState.collectAsStateWithLifecycle()
    val memoPagingItems = memoViewModel.memoPagingData.collectAsLazyPagingItems()
    val memoListState = rememberMemoListState()
    val sortUiState by memoViewModel.sortUiState.collectAsStateWithLifecycle()
    val sortSheetState = rememberDialogState()

    UpdateMemoListTodayEffect(state = memoListState)
    MemoListUndoSnackbarEffect(
        effect = memoViewModel.effect,
        snackbarHostState = snackbarHostState,
        onRestart = memoViewModel::restart,
        onRestore = memoViewModel::restore,
    )
    EntityDetailMemoTab(
        onEvent = { event ->
            when (event) {
                is EntityDetailMemoTabEvent.ClickSort -> sortSheetState.show()
                is EntityDetailMemoTabEvent.SelectSort -> memoViewModel.select(sort = event.sort)
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
        emptyDescription = emptyDescription,
        modifier = modifier,
        state = memoListState,
        sortSheetState = sortSheetState,
        memoPagingItems = memoPagingItems,
        syncUiStateProvider = { syncUiState },
        sortProvider = { sortUiState.sort },
        filterProvider = filterProvider,
        trailing = trailing,
    )
}
