package io.github.taetae98coding.diary.feature.search.ui.home.place

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.place.PlaceListEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.place.usecase.DeletePlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.RestorePlaceUseCase
import io.github.taetae98coding.diary.domain.search.usecase.SearchPlaceUseCase
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
internal class SearchHomePlaceViewModel(
    searchPlaceUseCase: SearchPlaceUseCase,
    private val deletePlaceUseCase: DeletePlaceUseCase,
    private val restorePlaceUseCase: RestorePlaceUseCase,
) : ViewModel() {
    private val resultState =
        SearchHomeResultState(
            search = { query, sort -> searchPlaceUseCase(parameter = SearchPlaceUseCase.Parameter(query = query, sort = sort)) },
        )

    val uiState: StateFlow<SearchHomeResultUiState> =
        resultState.uiState.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = SearchHomeResultUiState(),
        )
    val pagingData: Flow<PagingData<Place>> = resultState.pagingData.cachedIn(viewModelScope)

    private val _effect = Channel<PlaceListEffect>(Channel.BUFFERED)
    val effect: Flow<PlaceListEffect> = _effect.receiveAsFlow()

    fun updateQuery(query: String) {
        resultState.updateQuery(query = query)
    }

    fun select(sort: ListSort) {
        resultState.select(sort = sort)
    }

    fun delete(id: Uuid) {
        viewModelScope.launch {
            resultState.runOnce(id = id) {
                deletePlaceUseCase(parameter = id)
                    .onSuccess { _effect.send(PlaceListEffect.Deleted(id = id)) }
            }
        }
    }

    fun restore(id: Uuid) {
        viewModelScope.launch {
            resultState.runOnce(id = id) {
                restorePlaceUseCase(parameter = id)
            }
        }
    }
}
