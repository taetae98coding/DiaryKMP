package io.github.taetae98coding.diary.feature.web.ui.detail.memo

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.memo.list.MemoListEvent
import io.github.taetae98coding.diary.compose.memo.list.MemoListUndoSnackbarEffect
import io.github.taetae98coding.diary.compose.memo.list.UpdateMemoListTodayEffect
import io.github.taetae98coding.diary.compose.memo.list.rememberMemoListState
import io.github.taetae98coding.diary.feature.web.ui.detail.tab.WebDetailTab
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
internal fun WebDetailMemoContent(
    id: Uuid,
    viewModelStoreProvider: ViewModelStoreProvider,
    navigateToMemoAdd: () -> Unit,
    navigateToMemoDetail: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val viewModelStoreOwner = rememberViewModelStoreOwner(key = WebDetailTab.MEMO, provider = viewModelStoreProvider)

    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        val memoViewModel = koinViewModel<WebDetailMemoViewModel> { parametersOf(id) }
        val syncViewModel = koinViewModel<WebDetailMemoSyncViewModel>()
        val memoListUiState by syncViewModel.uiState.collectAsStateWithLifecycle()
        val memoPagingItems = memoViewModel.memoPagingData.collectAsLazyPagingItems()
        val memoListState = rememberMemoListState()
        val sort by memoViewModel.sort.collectAsStateWithLifecycle()
        val sortSheetState = rememberDialogState()

        UpdateMemoListTodayEffect(state = memoListState)
        MemoListUndoSnackbarEffect(
            effect = memoViewModel.effect,
            snackbarHostState = snackbarHostState,
            onRestart = memoViewModel::restart,
            onRestore = memoViewModel::restore,
        )
        WebDetailMemoTab(
            onEvent = { event ->
                when (event) {
                    is WebDetailMemoContentEvent.ClickAdd -> navigateToMemoAdd()
                    is WebDetailMemoContentEvent.ClickSort -> sortSheetState.show()
                    is WebDetailMemoContentEvent.SelectSort -> memoViewModel.select(sort = event.sort)
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
            modifier = modifier,
            state = memoListState,
            sortSheetState = sortSheetState,
            memoPagingItems = memoPagingItems,
            uiStateProvider = { memoListUiState },
            sortProvider = { sort },
        )
    }
}
