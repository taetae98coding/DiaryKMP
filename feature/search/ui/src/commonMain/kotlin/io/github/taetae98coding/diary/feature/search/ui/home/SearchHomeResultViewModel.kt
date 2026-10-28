@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import io.github.taetae98coding.diary.library.coroutines.flow.debounceSearchQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal abstract class SearchHomeResultViewModel<T : Any>(
    search: (query: String, sort: ListSort) -> Flow<Result<PagingData<T>>>,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val sort = MutableStateFlow(ListSort.TITLE)
    private val inProgressIds = mutableSetOf<Uuid>()

    private val appliedQuery: StateFlow<String> =
        query
            .debounceSearchQuery()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = "",
            )

    val uiState: StateFlow<SearchHomeResultUiState> =
        combine(sort, appliedQuery) { sortValue, appliedQuery ->
            SearchHomeResultUiState(sort = sortValue, appliedQuery = appliedQuery)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = SearchHomeResultUiState(sort = sort.value, appliedQuery = appliedQuery.value),
        )

    val pagingData: Flow<PagingData<T>> =
        combine(appliedQuery, sort) { query, sortValue -> query to sortValue }
            .flatMapLatest { (query, sortValue) -> search(query, sortValue).map { result -> result.getOrElse { PagingData.empty() } } }
            .cachedIn(viewModelScope)

    fun updateQuery(query: String) {
        this.query.value = query
    }

    fun select(sort: ListSort) {
        this.sort.value = sort
    }

    protected fun launchOnce(
        id: Uuid,
        block: suspend () -> Unit,
    ) {
        if (!inProgressIds.add(id)) return

        viewModelScope.launch {
            try {
                block()
            } finally {
                inProgressIds.remove(id)
            }
        }
    }
}
