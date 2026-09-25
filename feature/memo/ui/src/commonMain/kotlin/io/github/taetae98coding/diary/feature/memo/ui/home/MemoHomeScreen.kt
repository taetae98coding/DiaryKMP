package io.github.taetae98coding.diary.feature.memo.ui.home

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.shortcut.isAddShortcut
import io.github.taetae98coding.diary.compose.core.shortcut.keyShortcut
import io.github.taetae98coding.diary.compose.memo.list.MemoListEvent
import io.github.taetae98coding.diary.compose.memo.list.MemoListUndoSnackbarEffect
import io.github.taetae98coding.diary.compose.memo.list.UpdateMemoListTodayEffect
import io.github.taetae98coding.diary.compose.memo.list.rememberMemoListState
import kotlin.uuid.Uuid

@Composable
internal fun MemoHomeScreen(
    navigateToAdd: () -> Unit,
    navigateToDetail: (Uuid) -> Unit,
    navigateToFilter: () -> Unit,
    navigateToFinishedList: () -> Unit,
    navigateToSearch: () -> Unit,
    componentVisibleProvider: () -> MemoHomeScaffoldComponentVisible,
    listState: LazyListState,
    memoViewModel: MemoHomeViewModel,
    syncViewModel: MemoHomeSyncViewModel,
    modifier: Modifier = Modifier,
) {
    val memoListState = rememberMemoListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val memoPagingItems = memoViewModel.memoPagingData.collectAsLazyPagingItems()
    val filterUiState by memoViewModel.filterUiState.collectAsStateWithLifecycle()
    val memoListUiState by syncViewModel.uiState.collectAsStateWithLifecycle()
    val sort by memoViewModel.sort.collectAsStateWithLifecycle()
    val sortSheetState = rememberDialogState()

    UpdateMemoListTodayEffect(state = memoListState)
    MemoListUndoSnackbarEffect(
        effect = memoViewModel.effect,
        snackbarHostState = snackbarHostState,
        onRestart = memoViewModel::restart,
        onRestore = memoViewModel::restore,
    )

    MemoHomeScaffold(
        memoListState = memoListState,
        listState = listState,
        sortSheetState = sortSheetState,
        snackbarHostState = snackbarHostState,
        memoPagingItems = memoPagingItems,
        filterUiStateProvider = { filterUiState },
        memoListUiStateProvider = { memoListUiState },
        sortProvider = { sort },
        componentVisibleProvider = componentVisibleProvider,
        onEvent = { event ->
            when (event) {
                is MemoHomeScaffoldEvent.ClickAdd -> navigateToAdd()
                is MemoHomeScaffoldEvent.ClickFilter -> navigateToFilter()
                is MemoHomeScaffoldEvent.ClickFinishedList -> navigateToFinishedList()
                is MemoHomeScaffoldEvent.ClickSearch -> navigateToSearch()
                is MemoHomeScaffoldEvent.ClickSort -> sortSheetState.show()
                is MemoHomeScaffoldEvent.SelectSort -> memoViewModel.select(sort = event.sort)
            }
        },
        onMemoListEvent = { event ->
            when (event) {
                is MemoListEvent.ClickMemo -> navigateToDetail(event.id)
                is MemoListEvent.SwipeFinish -> memoViewModel.finish(id = event.id)
                is MemoListEvent.SwipeDelete -> memoViewModel.delete(id = event.id)
                is MemoListEvent.Refresh -> syncViewModel.refresh()
            }
        },
        modifier =
            modifier.keyShortcut(isEnableProvider = { componentVisibleProvider().isAddButtonVisible }) { keyEvent ->
                if (keyEvent.isAddShortcut()) {
                    navigateToAdd()
                    true
                } else {
                    false
                }
            },
    )
}
