@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.core.memo

import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.compose.memo.list.toMemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

public class MemoListState(
    pageMemo: (sort: ListSort) -> Flow<Result<PagingData<Memo>>>,
) {
    public val sortUiState: StateFlow<ListSortUiState>
        field = MutableStateFlow(ListSortUiState())

    public val memoPagingData: Flow<PagingData<MemoListItem>> =
        sortUiState.flatMapLatest { (sort) ->
            pageMemo(sort)
                .map { result -> result.getOrElse { PagingData.empty() } }
                .map { pagingData -> pagingData.toMemoListItem(sort = sort) }
        }

    public fun select(sort: ListSort) {
        sortUiState.value = ListSortUiState(sort = sort)
    }
}
