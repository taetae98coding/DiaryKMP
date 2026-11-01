package io.github.taetae98coding.diary.feature.search.ui.home.memo

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import io.github.taetae98coding.diary.compose.memo.list.MemoListEvent
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResultContent
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResultEvent
import kotlin.uuid.Uuid

@Composable
internal fun SearchHomeMemoContent(
    viewModel: SearchHomeMemoViewModel,
    pagingItems: LazyPagingItems<Memo>,
    listState: LazyListState,
    snackbarHostState: SnackbarHostState,
    navigateToDetail: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
) {
    SearchHomeResultContent(
        viewModel = viewModel,
        pagingItems = pagingItems,
        listState = listState,
    ) { state ->
        SearchHomeMemoUndoSnackbarEffect(
            onUndo = viewModel::undo,
            effect = viewModel.effect,
            snackbarHostState = snackbarHostState,
        )
        SearchHomeMemoList(
            onEvent = { event ->
                when (event) {
                    is SearchHomeResultEvent.ClickSort -> state.sortSheetState.show()
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
            listState = state.listState,
            sortSheetState = state.sortSheetState,
            memoPagingItems = state.pagingItems,
            query = state.uiState.appliedQuery,
            sortProvider = { state.uiState.sort },
        )
    }
}
