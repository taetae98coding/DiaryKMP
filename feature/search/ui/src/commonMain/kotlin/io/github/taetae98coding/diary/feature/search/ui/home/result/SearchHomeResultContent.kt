package io.github.taetae98coding.diary.feature.search.ui.home.result

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.list.ListQueryScrollEffect
import io.github.taetae98coding.diary.feature.search.ui.home.SearchHomeResultViewModel

@Composable
internal fun <T : Any> SearchHomeResultContent(
    viewModel: SearchHomeResultViewModel<T>,
    pagingItems: LazyPagingItems<T>,
    content: @Composable (state: SearchHomeResultContentState<T>) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val sortSheetState = rememberDialogState()

    SearchHomeQueryScrollEffect(
        listState = listState,
        queryProvider = { uiState.appliedQuery },
    )
    ListQueryScrollEffect(
        listState = listState,
        sortProvider = { uiState.sort },
        itemListProvider = { pagingItems.itemSnapshotList.items },
        isRefreshingProvider = { pagingItems.loadState.refresh is LoadState.Loading },
    )

    content(
        SearchHomeResultContentState(
            pagingItems = pagingItems,
            uiState = uiState,
            listState = listState,
            sortSheetState = sortSheetState,
        ),
    )
}
