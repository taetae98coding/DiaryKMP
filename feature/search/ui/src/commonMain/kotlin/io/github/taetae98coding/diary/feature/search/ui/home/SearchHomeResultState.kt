@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.library.coroutines.flow.debounceSearchQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlin.uuid.Uuid

internal class SearchHomeResultState<T : Any>(
    search: (query: String, sort: ListSort) -> Flow<Result<PagingData<T>>>,
) {
    private val query = MutableStateFlow("")
    private val sort = MutableStateFlow(ListSort.TITLE)
    private val inProgressIds = mutableSetOf<Uuid>()

    // 입력 중에 같은 검색어로 돌아오면 다시 조회하지 않도록 적용된 검색어가 바뀔 때만 내보낸다.
    private val appliedQuery: Flow<String> = query.debounceSearchQuery().distinctUntilChanged()

    val uiState: Flow<SearchHomeResultUiState> =
        combine(sort, appliedQuery) { sortValue, appliedQuery ->
            SearchHomeResultUiState(sort = sortValue, appliedQuery = appliedQuery)
        }

    val pagingData: Flow<PagingData<T>> =
        combine(appliedQuery, sort) { query, sortValue -> query to sortValue }
            .flatMapLatest { (query, sortValue) -> search(query, sortValue).map { result -> result.getOrElse { PagingData.empty() } } }

    fun updateQuery(query: String) {
        this.query.value = query
    }

    fun select(sort: ListSort) {
        this.sort.value = sort
    }

    suspend fun runOnce(
        id: Uuid,
        block: suspend () -> Unit,
    ) {
        if (!inProgressIds.add(id)) return

        try {
            block()
        } finally {
            inProgressIds.remove(id)
        }
    }
}
