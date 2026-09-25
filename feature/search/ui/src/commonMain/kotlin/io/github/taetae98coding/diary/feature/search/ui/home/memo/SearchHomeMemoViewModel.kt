@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.search.ui.home.memo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.domain.memo.usecase.DeleteMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.FinishMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestartMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.RestoreMemoUseCase
import io.github.taetae98coding.diary.domain.search.usecase.SearchMemoUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import io.github.taetae98coding.diary.library.coroutines.flow.debounceSearchQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class SearchHomeMemoViewModel(
    searchMemoUseCase: SearchMemoUseCase,
    private val finishMemoUseCase: FinishMemoUseCase,
    private val restartMemoUseCase: RestartMemoUseCase,
    private val deleteMemoUseCase: DeleteMemoUseCase,
    private val restoreMemoUseCase: RestoreMemoUseCase,
) : ViewModel() {
    private val query = MutableStateFlow("")

    val appliedQuery: StateFlow<String> =
        query
            .debounceSearchQuery()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = "",
            )

    val sort: StateFlow<ListSort>
        field = MutableStateFlow(ListSort.TITLE)

    val pagingData: Flow<PagingData<Memo>> =
        combine(appliedQuery, sort) { value, sortValue -> value to sortValue }
            .flatMapLatest { (value, sortValue) ->
                searchMemoUseCase(parameter = SearchMemoUseCase.Parameter(query = value, sort = sortValue))
            }.map { result -> result.getOrElse { PagingData.empty() } }
            .cachedIn(viewModelScope)

    private val _effect = Channel<SearchHomeMemoEffect>(Channel.BUFFERED)
    val effect: Flow<SearchHomeMemoEffect> = _effect.receiveAsFlow()

    fun updateQuery(query: String) {
        this.query.value = query
    }

    fun select(sort: ListSort) {
        this.sort.value = sort
    }

    fun finish(id: Uuid) {
        viewModelScope.launch {
            finishMemoUseCase(parameter = id)
                .onSuccess { _effect.send(SearchHomeMemoEffect.Finished(id = id)) }
        }
    }

    fun restart(id: Uuid) {
        viewModelScope.launch {
            restartMemoUseCase(parameter = id)
                .onSuccess { _effect.send(SearchHomeMemoEffect.Restarted(id = id)) }
        }
    }

    fun delete(id: Uuid) {
        viewModelScope.launch {
            deleteMemoUseCase(parameter = id)
                .onSuccess { _effect.send(SearchHomeMemoEffect.Deleted(id = id)) }
        }
    }

    fun undo(effect: SearchHomeMemoEffect) {
        viewModelScope.launch {
            when (effect) {
                is SearchHomeMemoEffect.Finished -> restartMemoUseCase(parameter = effect.id)
                is SearchHomeMemoEffect.Restarted -> finishMemoUseCase(parameter = effect.id)
                is SearchHomeMemoEffect.Deleted -> restoreMemoUseCase(parameter = effect.id)
            }
        }
    }
}
