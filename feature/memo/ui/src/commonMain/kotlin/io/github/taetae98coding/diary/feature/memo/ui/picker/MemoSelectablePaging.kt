@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.picker

import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.library.coroutines.flow.debounceReportedSearchQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal class MemoSelectablePaging<T : Any>(
    scope: CoroutineScope,
    page: (query: String) -> Flow<Result<PagingData<T>>>,
) {
    private val query =
        MutableSharedFlow<String>(
            replay = 1,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    val pagingData: Flow<PagingData<T>> =
        query
            .distinctUntilChanged()
            .debounceReportedSearchQuery()
            .flatMapLatest { value -> page(value) }
            .map { result -> result.getOrElse { PagingData.empty() } }
            .cachedIn(scope)

    val selectablePagingData: Flow<PagingData<T>> =
        flowOf("")
            .flatMapLatest { value -> page(value) }
            .map { result -> result.getOrElse { PagingData.empty() } }
            .cachedIn(scope)

    fun updateQuery(query: String) {
        this.query.tryEmit(query)
    }
}
