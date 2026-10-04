package io.github.taetae98coding.diary.data.core.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

public fun <Key : Any, Local : Any, Model : Any> pagingFlow(
    pagingSourceFactory: () -> PagingSource<Key, Local>,
    transform: suspend (Local) -> Model,
): Flow<PagingData<Model>> =
    Pager(
        config = PagingConfig(pageSize = PAGE_SIZE),
        pagingSourceFactory = pagingSourceFactory,
    ).flow.map { pagingData ->
        pagingData.map(transform)
    }
