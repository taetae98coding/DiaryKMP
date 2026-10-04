package io.github.taetae98coding.diary.feature.search.ui.home.result

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.list.ListQueryScrollEffect
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.search.ui.home.SearchHomeQueryEffect
import io.github.taetae98coding.diary.feature.search.ui.home.SearchHomeResultViewModel

@Composable
internal fun <T : Any, VM : SearchHomeResultViewModel<T>> SearchHomeResultContent(
    type: SearchHomeType,
    queryState: TextFieldState,
    viewModelStoreProvider: ViewModelStoreProvider,
    viewModel: @Composable () -> VM,
    content: @Composable (viewModel: VM, state: SearchHomeResultContentState<T>) -> Unit,
) {
    val viewModelStoreOwner = rememberViewModelStoreOwner(key = type, provider = viewModelStoreProvider)

    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        val resultViewModel = viewModel()

        // 결과를 받기 전에 지금 질의를 먼저 알려, 다시 나타난 유형이 떠나기 전의 결과를 건너뛰게 한다.
        SearchHomeQueryEffect(
            queryState = queryState,
            onQueryShow = resultViewModel::showQuery,
            onQueryChange = resultViewModel::updateQuery,
        )

        val pagingItems = resultViewModel.pagingData.collectAsLazyPagingItems()
        val uiState by resultViewModel.uiState.collectAsStateWithLifecycle()
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
            resultViewModel,
            SearchHomeResultContentState(
                pagingItems = pagingItems,
                uiState = uiState,
                listState = listState,
                sortSheetState = sortSheetState,
            ),
        )
    }
}
