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
import io.github.taetae98coding.diary.feature.search.ui.home.SearchHomeResultState
import io.github.taetae98coding.diary.feature.search.ui.home.SearchHomeResultUiState
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    private val resultState =
        SearchHomeResultState(
            search = { query, sort -> searchMemoUseCase(parameter = SearchMemoUseCase.Parameter(query = query, sort = sort)) },
        )

    val uiState: StateFlow<SearchHomeResultUiState> =
        resultState.uiState.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = SearchHomeResultUiState(),
        )
    val pagingData: Flow<PagingData<Memo>> = resultState.pagingData.cachedIn(viewModelScope)

    private val _effect = Channel<SearchHomeMemoEffect>(Channel.BUFFERED)
    val effect: Flow<SearchHomeMemoEffect> = _effect.receiveAsFlow()

    fun updateQuery(query: String) {
        resultState.updateQuery(query = query)
    }

    fun select(sort: ListSort) {
        resultState.select(sort = sort)
    }

    fun finish(id: Uuid) {
        viewModelScope.launch {
            resultState.runOnce(id = id) {
                finishMemoUseCase(parameter = id)
                    .onSuccess { _effect.send(SearchHomeMemoEffect.Finished(id = id)) }
            }
        }
    }

    fun restart(id: Uuid) {
        viewModelScope.launch {
            resultState.runOnce(id = id) {
                restartMemoUseCase(parameter = id)
                    .onSuccess { _effect.send(SearchHomeMemoEffect.Restarted(id = id)) }
            }
        }
    }

    fun delete(id: Uuid) {
        viewModelScope.launch {
            resultState.runOnce(id = id) {
                deleteMemoUseCase(parameter = id)
                    .onSuccess { _effect.send(SearchHomeMemoEffect.Deleted(id = id)) }
            }
        }
    }

    fun undo(effect: SearchHomeMemoEffect) {
        viewModelScope.launch {
            resultState.runOnce(id = effect.targetId()) {
                when (effect) {
                    is SearchHomeMemoEffect.Finished -> restartMemoUseCase(parameter = effect.id)
                    is SearchHomeMemoEffect.Restarted -> finishMemoUseCase(parameter = effect.id)
                    is SearchHomeMemoEffect.Deleted -> restoreMemoUseCase(parameter = effect.id)
                }
            }
        }
    }

    private fun SearchHomeMemoEffect.targetId(): Uuid =
        when (this) {
            is SearchHomeMemoEffect.Finished -> id
            is SearchHomeMemoEffect.Restarted -> id
            is SearchHomeMemoEffect.Deleted -> id
        }
}
