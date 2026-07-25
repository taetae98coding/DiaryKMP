package io.github.taetae98coding.diary.compose.core.paging

import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

public fun LazyPagingItems<*>.isLoadedEmpty(): Boolean = itemCount == 0 && loadState.refresh !is LoadState.Loading

public fun LazyPagingItems<*>.isConfirmedEmpty(): Boolean = itemCount == 0 && loadState.refresh is LoadState.NotLoading
