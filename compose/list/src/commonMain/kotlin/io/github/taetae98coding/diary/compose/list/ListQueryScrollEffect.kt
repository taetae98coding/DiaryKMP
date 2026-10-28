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
import io.github.taetae98coding.diary.core.model.list.ListSort
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter

/**
 * [filterProvider]가 null을 돌려주면 조건을 아직 불러오지 않은 것으로 본다.
 */
@Composable
public fun ListQueryScrollEffect(
    listState: LazyListState,
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
    filterProvider: () -> Any? = { Unit },
) {
    ScrollToTopEffect(
        scrollableState = listState,
        sortProvider = sortProvider,
        filterProvider = filterProvider,
        scrollToTop = { listState.requestScrollToItem(0) },
    )
}

@Composable
public fun ListQueryScrollEffect(
    gridState: LazyGridState,
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
    filterProvider: () -> Any? = { Unit },
) {
    ScrollToTopEffect(
        scrollableState = gridState,
        sortProvider = sortProvider,
        filterProvider = filterProvider,
        scrollToTop = { gridState.requestScrollToItem(0) },
    )
}

@Composable
public fun ListQueryScrollEffect(
    staggeredGridState: LazyStaggeredGridState,
    sortProvider: () -> ListSort = { ListSort.DEFAULT },
    filterProvider: () -> Any? = { Unit },
) {
    ScrollToTopEffect(
        scrollableState = staggeredGridState,
        sortProvider = sortProvider,
        filterProvider = filterProvider,
        scrollToTop = { staggeredGridState.requestScrollToItem(0) },
    )
}

@Composable
private fun ScrollToTopEffect(
    scrollableState: ScrollableState,
    sortProvider: () -> ListSort,
    filterProvider: () -> Any?,
    scrollToTop: () -> Unit,
) {
    val latestSortProvider by rememberUpdatedState(sortProvider)
    val latestFilterProvider by rememberUpdatedState(filterProvider)

    LaunchedEffect(scrollableState) {
        snapshotFlow { ListQuery(sort = latestSortProvider(), filter = latestFilterProvider()) }
            .filter { query -> query.filter != null }
            .drop(1)
            .collect { scrollToTop() }
    }
}

private data class ListQuery(
    val sort: ListSort,
    val filter: Any?,
)
