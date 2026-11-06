package io.github.taetae98coding.diary.feature.search.ui.home.web

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.web.WebListEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.search.usecase.SearchWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.DeleteWebUseCase
import io.github.taetae98coding.diary.domain.web.usecase.RestoreWebUseCase
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
internal class SearchHomeWebViewModel(
    searchWebUseCase: SearchWebUseCase,
    private val deleteWebUseCase: DeleteWebUseCase,
    private val restoreWebUseCase: RestoreWebUseCase,
) : ViewModel() {
    private val resultState =
        SearchHomeResultState(
            search = { query, sort -> searchWebUseCase(parameter = SearchWebUseCase.Parameter(query = query, sort = sort)) },
        )

    val uiState: StateFlow<SearchHomeResultUiState> =
        resultState.uiState.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = SearchHomeResultUiState(),
        )
    val pagingData: Flow<PagingData<Web>> = resultState.pagingData.cachedIn(viewModelScope)

    private val _effect = Channel<WebListEffect>(Channel.BUFFERED)
    val effect: Flow<WebListEffect> = _effect.receiveAsFlow()

    fun updateQuery(query: String) {
        resultState.updateQuery(query = query)
    }

    fun select(sort: ListSort) {
        resultState.select(sort = sort)
    }

    fun delete(id: Uuid) {
        viewModelScope.launch {
            resultState.runOnce(id = id) {
                deleteWebUseCase(parameter = id)
                    .onSuccess { _effect.send(WebListEffect.Deleted(id = id)) }
            }
        }
    }

    fun restore(id: Uuid) {
        viewModelScope.launch {
            resultState.runOnce(id = id) {
                restoreWebUseCase(parameter = id)
            }
        }
    }
}
