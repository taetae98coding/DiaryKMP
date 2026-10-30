package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import io.mockk.coEvery
import io.mockk.every
import io.mockk.spyk
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
        pagingSourceFactory = { appendFailingPagingSource(firstPage = firstPage) },
    ).flow

// 무효화 추적은 PagingSource의 실제 구현을 써야 하므로 spy로 만들고, 조회 결과만 스텁한다.
private fun <T : Any> appendFailingPagingSource(firstPage: List<T>): PagingSource<Int, T> =
    spyk<PagingSource<Int, T>>().apply {
        coEvery { load(any()) } answers {
            if (firstArg<PagingSource.LoadParams<Int>>() is PagingSource.LoadParams.Refresh) {
                PagingSource.LoadResult.Page(data = firstPage, prevKey = null, nextKey = 1, itemsBefore = 0, itemsAfter = 1)
            } else {
                PagingSource.LoadResult.Error(IllegalStateException())
            }
        }
        every { getRefreshKey(any()) } returns null
    }
