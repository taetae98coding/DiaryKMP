package io.github.taetae98coding.diary.feature.search.ui.home.result

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import io.github.taetae98coding.diary.compose.list.ListQueryScrollEffect
import io.github.taetae98coding.diary.feature.search.ui.home.SearchHomeResultViewModel

// 결과 영역은 보고 있는 유형만 구성하므로, 다른 유형을 보는 동안 바뀐 질의와 정렬도 놓치지 않게 화면에서 유형마다 둔다.
@Composable
internal fun <T : Any> SearchHomeResultScrollEffect(
    viewModel: SearchHomeResultViewModel<T>,
    pagingItems: LazyPagingItems<T>,
    listState: LazyListState,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
}
