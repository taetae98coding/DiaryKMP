package io.github.taetae98coding.diary.feature.place.ui

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.Flow

internal fun <T : Any> appendFailingPagingData(firstPage: List<T>): Flow<PagingData<T>> =
    Pager(
        config =
            PagingConfig(
                pageSize = firstPage.size,
                prefetchDistance = 0,
                enablePlaceholders = true,
                initialLoadSize = firstPage.size,
            ),
        pagingSourceFactory = { AppendFailingPagingSource(firstPage = firstPage) },
    ).flow

private class AppendFailingPagingSource<T : Any>(
    private val firstPage: List<T>,
) : PagingSource<Int, T>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> =
        if (params is LoadParams.Refresh) {
            LoadResult.Page(data = firstPage, prevKey = null, nextKey = 1, itemsBefore = 0, itemsAfter = 1)
        } else {
            LoadResult.Error(IllegalStateException())
        }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? = null
}
