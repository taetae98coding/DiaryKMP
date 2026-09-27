package io.github.taetae98coding.diary.feature.search.ui.home.memo

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.TextFieldState
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
import io.github.taetae98coding.diary.compose.list.ListQueryScrollEffect
import io.github.taetae98coding.diary.compose.memo.list.MemoListEvent
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.search.ui.home.SearchHomeQueryEffect
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeQueryScrollEffect
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResultEvent
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.Uuid

@Composable
internal fun SearchHomeMemoContent(
    queryState: TextFieldState,
    viewModelStoreProvider: ViewModelStoreProvider,
    navigateToDetail: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val viewModelStoreOwner = rememberViewModelStoreOwner(key = SearchHomeType.MEMO, provider = viewModelStoreProvider)

    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        val viewModel = koinViewModel<SearchHomeMemoViewModel>()

        // 결과를 받기 전에 지금 질의를 먼저 알려, 다시 나타난 유형이 떠나기 전의 결과를 건너뛰게 한다.
        SearchHomeQueryEffect(
            queryState = queryState,
            onQueryShow = viewModel::showQuery,
            onQueryChange = viewModel::updateQuery,
        )

        val memoPagingItems = viewModel.pagingData.collectAsLazyPagingItems()
        val appliedQuery by viewModel.appliedQuery.collectAsStateWithLifecycle()
        val listState = rememberLazyListState()
        val sort by viewModel.sort.collectAsStateWithLifecycle()
        val sortSheetState = rememberDialogState()

        SearchHomeMemoUndoSnackbarEffect(
            onUndo = viewModel::undo,
            effect = viewModel.effect,
            snackbarHostState = snackbarHostState,
        )
        SearchHomeQueryScrollEffect(
            listState = listState,
            queryProvider = { appliedQuery },
        )
        ListQueryScrollEffect(
            listState = listState,
            sortProvider = { sort },
            itemListProvider = { memoPagingItems.itemSnapshotList.items },
        )
        SearchHomeMemoList(
            onEvent = { event ->
                when (event) {
                    is SearchHomeResultEvent.ClickSort -> sortSheetState.show()
                    is SearchHomeResultEvent.SelectSort -> viewModel.select(sort = event.sort)
                }
            },
            onMemoListEvent = { event ->
                when (event) {
                    is MemoListEvent.ClickMemo -> navigateToDetail(event.id)
                    is MemoListEvent.SwipeFinish -> viewModel.finish(id = event.id)
                    is MemoListEvent.SwipeRestart -> viewModel.restart(id = event.id)
                    is MemoListEvent.SwipeDelete -> viewModel.delete(id = event.id)
                    is MemoListEvent.Refresh -> Unit
                }
            },
            modifier = modifier,
            listState = listState,
            sortSheetState = sortSheetState,
            memoPagingItems = memoPagingItems,
            query = appliedQuery,
            sortProvider = { sort },
        )
    }
}
