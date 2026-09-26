package io.github.taetae98coding.diary.feature.search.ui.home.web

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.web.WebListEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.search.usecase.SearchWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.DeleteWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.RestoreWebUseCase
import io.github.taetae98coding.diary.feature.search.ui.home.SearchHomeQueryInput
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class SearchHomeWebViewModel(
    searchWebUseCase: SearchWebUseCase,
    private val deleteWebUseCase: DeleteWebUseCase,
    private val restoreWebUseCase: RestoreWebUseCase,
) : ViewModel() {
    private val query = SearchHomeQueryInput(scope = viewModelScope)

    val appliedQuery: StateFlow<String> = query.appliedQuery

    val sort: StateFlow<ListSort>
        field = MutableStateFlow(ListSort.TITLE)

    val pagingData: Flow<PagingData<Web>> =
        query.pagingData(sort = sort) { value, sortValue ->
            searchWebUseCase(parameter = SearchWebUseCase.Parameter(query = value, sort = sortValue))
                .map { result -> result.getOrElse { PagingData.empty() } }
        }

    private val _effect = Channel<WebListEffect>(Channel.BUFFERED)
    val effect: Flow<WebListEffect> = _effect.receiveAsFlow()

    fun showQuery(query: String) {
        this.query.show(query = query)
    }

    fun updateQuery(query: String) {
        this.query.update(query = query)
    }

    fun select(sort: ListSort) {
        this.sort.value = sort
    }

    fun delete(id: Uuid) {
        viewModelScope.launch {
            deleteWebUseCase(parameter = id)
                .onSuccess { _effect.send(WebListEffect.Deleted(id = id)) }
        }
    }

    fun restore(id: Uuid) {
        viewModelScope.launch {
            restoreWebUseCase(parameter = id)
        }
    }
}
