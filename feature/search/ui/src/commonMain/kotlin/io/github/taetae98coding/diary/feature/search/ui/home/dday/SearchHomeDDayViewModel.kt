package io.github.taetae98coding.diary.feature.search.ui.home.dday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.dday.DDayListEffect
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.domain.dday.usecase.DeleteDDayUseCase
import io.github.taetae98coding.diary.domain.dday.usecase.RestoreDDayUseCase
import io.github.taetae98coding.diary.domain.search.usecase.SearchDDayUseCase
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
internal class SearchHomeDDayViewModel(
    searchDDayUseCase: SearchDDayUseCase,
    private val deleteDDayUseCase: DeleteDDayUseCase,
    private val restoreDDayUseCase: RestoreDDayUseCase,
) : ViewModel() {
    private val resultState =
        SearchHomeResultState(
            search = { query, sort -> searchDDayUseCase(parameter = SearchDDayUseCase.Parameter(query = query, sort = sort)) },
        )

    val uiState: StateFlow<SearchHomeResultUiState> =
        resultState.uiState.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = SearchHomeResultUiState(),
        )
    val pagingData: Flow<PagingData<DDay>> = resultState.pagingData.cachedIn(viewModelScope)

    private val _effect = Channel<DDayListEffect>(Channel.BUFFERED)
    val effect: Flow<DDayListEffect> = _effect.receiveAsFlow()

    fun updateQuery(query: String) {
        resultState.updateQuery(query = query)
    }

    fun select(sort: ListSort) {
        resultState.select(sort = sort)
    }

    fun delete(id: Uuid) {
        viewModelScope.launch {
            resultState.runOnce(id = id) {
                deleteDDayUseCase(parameter = id)
                    .onSuccess { _effect.send(DDayListEffect.Deleted(id = id)) }
            }
        }
    }

    fun restore(id: Uuid) {
        viewModelScope.launch {
            resultState.runOnce(id = id) {
                restoreDDayUseCase(parameter = id)
            }
        }
    }
}
