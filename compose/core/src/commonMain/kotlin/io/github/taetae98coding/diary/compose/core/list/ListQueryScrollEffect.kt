package io.github.taetae98coding.diary.compose.core.list

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
public fun ListQueryScrollEffect(
    listState: LazyListState,
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
    filterProvider: () -> Any? = { Unit },
    itemListProvider: () -> List<Any?> = { emptyList() },
) {
    ScrollToTopEffect(
        scrollableState = listState,
        sortProvider = sortProvider,
        filterProvider = filterProvider,
        itemListProvider = itemListProvider,
        scrollToTop = { listState.requestScrollToItem(0) },
    )
}

@Composable
public fun ListQueryScrollEffect(
    gridState: LazyGridState,
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
    filterProvider: () -> Any? = { Unit },
    itemListProvider: () -> List<Any?> = { emptyList() },
) {
    ScrollToTopEffect(
        scrollableState = gridState,
        sortProvider = sortProvider,
        filterProvider = filterProvider,
        itemListProvider = itemListProvider,
        scrollToTop = { gridState.requestScrollToItem(0) },
    )
}

@Composable
public fun ListQueryScrollEffect(
    staggeredGridState: LazyStaggeredGridState,
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
    filterProvider: () -> Any? = { Unit },
    itemListProvider: () -> List<Any?> = { emptyList() },
) {
    ScrollToTopEffect(
        scrollableState = staggeredGridState,
        sortProvider = sortProvider,
        filterProvider = filterProvider,
        itemListProvider = itemListProvider,
        scrollToTop = { staggeredGridState.requestScrollToItem(0) },
    )
}

@Composable
private fun ScrollToTopEffect(
    scrollableState: ScrollableState,
    sortProvider: () -> ListSort,
    filterProvider: () -> Any?,
    itemListProvider: () -> List<Any?>,
    scrollToTop: () -> Unit,
) {
    val latestSortProvider by rememberUpdatedState(sortProvider)
    val latestFilterProvider by rememberUpdatedState(filterProvider)
    val latestItemListProvider by rememberUpdatedState(itemListProvider)

    LaunchedEffect(scrollableState) {
        // 조회 조건을 바꾼 시점에는 이전 조건으로 조회한 목록이 그대로 놓여 있고, 그때 올리면 새 목록이 도착할 때 목록이
        // 이전에 보던 항목을 따라 자리를 되찾는다. 그래서 지금 놓인 목록과 그 목록을 조회한 조건을 함께 들고 있다가,
        // 바뀐 조건으로 조회한 목록이 도착했을 때 올린다. 조건과 목록을 한 번에 읽어야 도착을 놓치지 않는다.
        var placedQuery: ListQuery? = null
        var placedItemList: List<Any?>? = null

        snapshotFlow { ListQuery(sort = latestSortProvider(), filter = latestFilterProvider()) to latestItemListProvider() }
            .collect { (query, itemList) ->
                if (placedItemList == null) {
                    placedQuery = query
                    placedItemList = itemList

                    return@collect
                }

                if (itemList == placedItemList) return@collect

                val previousQuery = placedQuery

                placedQuery = query
                placedItemList = itemList

                if (query != previousQuery) {
                    scrollToTop()
                }
            }
    }
}

private data class ListQuery(
    val sort: ListSort,
    val filter: Any?,
)
