package io.github.taetae98coding.diary.compose.core.sort

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import io.github.taetae98coding.diary.core.model.list.ListSort

@Composable
public fun ListSortScrollEffect(
    listState: LazyListState,
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
    itemListProvider: () -> List<Any?> = { emptyList() },
) {
    ScrollToTopEffect(
        scrollableState = listState,
        sortProvider = sortProvider,
        itemListProvider = itemListProvider,
        scrollToTop = { listState.requestScrollToItem(0) },
    )
}

@Composable
public fun ListSortScrollEffect(
    gridState: LazyGridState,
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
    itemListProvider: () -> List<Any?> = { emptyList() },
) {
    ScrollToTopEffect(
        scrollableState = gridState,
        sortProvider = sortProvider,
        itemListProvider = itemListProvider,
        scrollToTop = { gridState.requestScrollToItem(0) },
    )
}

@Composable
public fun ListSortScrollEffect(
    staggeredGridState: LazyStaggeredGridState,
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
    itemListProvider: () -> List<Any?> = { emptyList() },
) {
    ScrollToTopEffect(
        scrollableState = staggeredGridState,
        sortProvider = sortProvider,
        itemListProvider = itemListProvider,
        scrollToTop = { staggeredGridState.requestScrollToItem(0) },
    )
}

@Composable
private fun ScrollToTopEffect(
    scrollableState: ScrollableState,
    sortProvider: () -> ListSort,
    itemListProvider: () -> List<Any?>,
    scrollToTop: () -> Unit,
) {
    val latestSortProvider by rememberUpdatedState(sortProvider)
    val latestItemListProvider by rememberUpdatedState(itemListProvider)

    LaunchedEffect(scrollableState) {
        // 정렬을 바꾼 시점에는 이전 정렬로 조회한 목록이 그대로 놓여 있고, 그때 올리면 새 목록이 도착할 때 목록이
        // 이전에 보던 항목을 따라 자리를 되찾는다. 그래서 지금 놓인 목록과 그 목록을 조회한 정렬을 함께 들고 있다가,
        // 바뀐 정렬로 조회한 목록이 도착했을 때 올린다. 정렬과 목록을 한 번에 읽어야 도착을 놓치지 않는다.
        var placedSort: ListSort? = null
        var placedItemList: List<Any?>? = null

        snapshotFlow { latestSortProvider() to latestItemListProvider() }
            .collect { (sort, itemList) ->
                if (placedItemList == null) {
                    placedSort = sort
                    placedItemList = itemList

                    return@collect
                }

                if (itemList == placedItemList) return@collect

                val previousSort = placedSort

                placedSort = sort
                placedItemList = itemList

                if (sort != previousSort) {
                    scrollToTop()
                }
            }
    }
}
