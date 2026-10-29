package io.github.taetae98coding.diary.compose.list

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import io.github.taetae98coding.diary.core.model.list.ListSort

/**
 * [filterProvider]가 null을 돌려주면 조건을 아직 불러오지 않은 것으로 본다.
 */
@Composable
public fun ListQueryScrollEffect(
    listState: LazyListState,
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
    filterProvider: () -> Any? = { Unit },
    itemListProvider: () -> List<Any?> = { emptyList() },
    isRefreshingProvider: () -> Boolean = { false },
) {
    ScrollToTopEffect(
        scrollableState = listState,
        sortProvider = sortProvider,
        filterProvider = filterProvider,
        itemListProvider = itemListProvider,
        isRefreshingProvider = isRefreshingProvider,
        scrollToTop = { listState.requestScrollToItem(0) },
    )
}

@Composable
public fun ListQueryScrollEffect(
    gridState: LazyGridState,
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
    filterProvider: () -> Any? = { Unit },
    itemListProvider: () -> List<Any?> = { emptyList() },
    isRefreshingProvider: () -> Boolean = { false },
) {
    ScrollToTopEffect(
        scrollableState = gridState,
        sortProvider = sortProvider,
        filterProvider = filterProvider,
        itemListProvider = itemListProvider,
        isRefreshingProvider = isRefreshingProvider,
        scrollToTop = { gridState.requestScrollToItem(0) },
    )
}

@Composable
public fun ListQueryScrollEffect(
    staggeredGridState: LazyStaggeredGridState,
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
    filterProvider: () -> Any? = { Unit },
    itemListProvider: () -> List<Any?> = { emptyList() },
    isRefreshingProvider: () -> Boolean = { false },
) {
    ScrollToTopEffect(
        scrollableState = staggeredGridState,
        sortProvider = sortProvider,
        filterProvider = filterProvider,
        itemListProvider = itemListProvider,
        isRefreshingProvider = isRefreshingProvider,
        scrollToTop = { staggeredGridState.requestScrollToItem(0) },
    )
}

@Composable
private fun ScrollToTopEffect(
    scrollableState: ScrollableState,
    sortProvider: () -> ListSort,
    filterProvider: () -> Any?,
    itemListProvider: () -> List<Any?>,
    isRefreshingProvider: () -> Boolean,
    scrollToTop: () -> Unit,
) {
    val latestSortProvider by rememberUpdatedState(sortProvider)
    val latestFilterProvider by rememberUpdatedState(filterProvider)
    val latestItemListProvider by rememberUpdatedState(itemListProvider)
    val latestIsRefreshingProvider by rememberUpdatedState(isRefreshingProvider)

    LaunchedEffect(scrollableState) {
        // 조회 조건을 바꾼 시점에는 이전 조건으로 조회한 목록이 그대로 놓여 있고, 그때 올리면 새 목록이 도착할 때 목록이
        // 이전에 보던 항목을 따라 자리를 되찾는다. 그래서 지금 놓인 목록과 그 목록을 조회한 조건을 함께 들고 있다가,
        // 바뀐 조건으로 조회한 목록이 도착했을 때 올린다. 조건과 목록을 한 번에 읽어야 도착을 놓치지 않는다.
        val placement = ListPlacement()

        snapshotFlow { ScrollSnapshot(query = ListQuery(sort = latestSortProvider(), filter = latestFilterProvider()), itemList = latestItemListProvider(), isRefreshing = latestIsRefreshingProvider()) }
            .collect { snapshot ->
                if (placement.shouldScrollToTop(snapshot = snapshot, latestItemListProvider = latestItemListProvider)) {
                    scrollToTop()
                }
            }
    }
}

// 지금 놓인 목록과 그 목록을 조회한 조건을 함께 들고, 바뀐 조건으로 조회한 목록이 놓였는지 판단한다.
private class ListPlacement {
    private var placedQuery: ListQuery? = null
    private var placedItemList: List<Any?>? = null

    // 바뀐 조건의 조회가 이전과 같은 목록을 돌려주면 목록이 바뀌지 않아 도착을 알 수 없다. 조건을 바꾼 뒤 시작된 조회가 끝나면 그 결과가 놓인 것으로 본다.
    private var isRefreshStartedAfterQueryChange = false

    suspend fun shouldScrollToTop(
        snapshot: ScrollSnapshot,
        latestItemListProvider: () -> List<Any?>,
    ): Boolean {
        val previousQuery = placedQuery

        return when {
            previousQuery == null || previousQuery.filter == null -> {
                place(query = snapshot.query, itemList = snapshot.itemList)
                false
            }

            snapshot.itemList != placedItemList -> {
                place(query = snapshot.query, itemList = snapshot.itemList)
                snapshot.query != previousQuery
            }

            snapshot.query == previousQuery -> {
                isRefreshStartedAfterQueryChange = false
                false
            }

            snapshot.isRefreshing -> {
                isRefreshStartedAfterQueryChange = true
                false
            }

            isRefreshStartedAfterQueryChange -> placeSameResult(query = snapshot.query, latestItemListProvider = latestItemListProvider)

            else -> false
        }
    }

    // 조회가 끝난 뒤 목록이 한 프레임 늦게 놓일 수 있으므로, 다음 프레임에도 목록이 그대로일 때만 같은 결과로 본다.
    private suspend fun placeSameResult(
        query: ListQuery,
        latestItemListProvider: () -> List<Any?>,
    ): Boolean {
        withFrameNanos { }
        if (latestItemListProvider() != placedItemList) return false

        place(query = query, itemList = placedItemList)
        return true
    }

    private fun place(
        query: ListQuery,
        itemList: List<Any?>?,
    ) {
        placedQuery = query
        placedItemList = itemList
        isRefreshStartedAfterQueryChange = false
    }
}

private data class ScrollSnapshot(
    val query: ListQuery,
    val itemList: List<Any?>,
    val isRefreshing: Boolean,
)

private data class ListQuery(
    val sort: ListSort,
    val filter: Any?,
)
