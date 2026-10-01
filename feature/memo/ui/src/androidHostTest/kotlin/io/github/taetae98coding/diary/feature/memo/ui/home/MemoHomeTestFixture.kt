package io.github.taetae98coding.diary.feature.memo.ui.home

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem

internal fun memoPagingDataOf(itemList: List<MemoListItem>): PagingData<MemoListItem> =
    PagingData.from(
        data = itemList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = true),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
    )

internal fun loadingMemoPagingData(): PagingData<MemoListItem> =
    PagingData.from(
        data = emptyList(),
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.Loading,
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            ),
    )

internal fun memoPagingDataOf(
    itemList: List<MemoListItem>,
    isRefreshing: Boolean,
): PagingData<MemoListItem> {
    val loaded = LoadState.NotLoading(endOfPaginationReached = true)

    return PagingData.from(
        data = itemList,
        sourceLoadStates = LoadStates(refresh = if (isRefreshing) LoadState.Loading else loaded, prepend = loaded, append = loaded),
    )
}
