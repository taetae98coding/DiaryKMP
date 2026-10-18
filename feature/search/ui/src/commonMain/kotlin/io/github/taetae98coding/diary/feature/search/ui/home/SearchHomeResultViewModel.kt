package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

internal abstract class SearchHomeResultViewModel<T : Any>(
    search: (query: String, sort: ListSort) -> Flow<Result<PagingData<T>>>,
) : ViewModel() {
    private val query = SearchHomeQueryInput(scope = viewModelScope)
    private val sort = MutableStateFlow(ListSort.TITLE)
    private val inProgressIds = mutableSetOf<Uuid>()

    val uiState: StateFlow<SearchHomeResultUiState> =
        combine(sort, query.appliedQuery) { sortValue, appliedQuery ->
            SearchHomeResultUiState(sort = sortValue, appliedQuery = appliedQuery)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = SearchHomeResultUiState(sort = sort.value, appliedQuery = query.appliedQuery.value),
        )

    val pagingData: Flow<PagingData<T>> =
        query.pagingData(sort = sort) { value, sortValue ->
            search(value, sortValue).map { result -> result.getOrElse { PagingData.empty() } }
        }

    fun showQuery(query: String) {
        this.query.show(query = query)
    }

    fun updateQuery(query: String) {
        this.query.update(query = query)
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
