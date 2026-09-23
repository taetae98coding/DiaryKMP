package io.github.taetae98coding.diary.feature.tag.ui.detail.memo

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
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeEffect
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeState
import io.github.taetae98coding.diary.feature.tag.ui.detail.tab.TagDetailTab
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
internal fun TagDetailMemoContent(
    id: Uuid,
    viewModelStoreProvider: ViewModelStoreProvider,
    navigateToMemoDetail: (Uuid) -> Unit,
    navigateToMemoFinishedList: () -> Unit,
    scopeState: TagDetailScopeState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val viewModelStoreOwner = rememberViewModelStoreOwner(key = TagDetailTab.MEMO, provider = viewModelStoreProvider)

    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        val memoViewModel = koinViewModel<TagDetailMemoViewModel> { parametersOf(id) }
        val syncViewModel = koinViewModel<TagDetailMemoSyncViewModel>()
        val memoListUiState by syncViewModel.uiState.collectAsStateWithLifecycle()
        val memoPagingItems = memoViewModel.memoPagingData.collectAsLazyPagingItems()
        val memoListState = rememberMemoListState()
        val sort by memoViewModel.sort.collectAsStateWithLifecycle()
        val sortSheetState = rememberDialogState()

        TagDetailScopeEffect(
            onSelect = { scope -> memoViewModel.select(scope = scope) },
            state = scopeState,
        )
        UpdateMemoListTodayEffect(state = memoListState)
        MemoListUndoSnackbarEffect(
            effect = memoViewModel.effect,
            snackbarHostState = snackbarHostState,
            onRestart = memoViewModel::restart,
            onRestore = memoViewModel::restore,
        )
        TagDetailMemoTab(
            onEvent = { event ->
                when (event) {
                    is TagDetailMemoContentEvent.ClickFinishedList -> navigateToMemoFinishedList()
                    is TagDetailMemoContentEvent.ClickSort -> sortSheetState.show()
                    is TagDetailMemoContentEvent.SelectSort -> memoViewModel.select(sort = event.sort)
                }
            },
            onMemoListEvent = { event ->
                when (event) {
                    is MemoListEvent.ClickMemo -> navigateToMemoDetail(event.id)
                    is MemoListEvent.SwipeFinish -> memoViewModel.finish(id = event.id)
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
