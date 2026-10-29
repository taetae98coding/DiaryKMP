@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.picker

import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.library.coroutines.flow.debounceSearchQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal class MemoSelectablePaging<T : Any>(
    scope: CoroutineScope,
    page: (query: String) -> Flow<Result<PagingData<T>>>,
) {
    private val query = MutableStateFlow("")

    val pagingData: Flow<PagingData<T>> =
        query
            .debounceSearchQuery()
            .flatMapLatest { value -> page(value) }
            .map { result -> result.getOrElse { PagingData.empty() } }
            .cachedIn(scope)

    val selectablePagingData: Flow<PagingData<T>> =
        flowOf("")
            .flatMapLatest { value -> page(value) }
            .map { result -> result.getOrElse { PagingData.empty() } }
            .cachedIn(scope)

    fun updateQuery(query: String) {
        this.query.value = query
    }
}
