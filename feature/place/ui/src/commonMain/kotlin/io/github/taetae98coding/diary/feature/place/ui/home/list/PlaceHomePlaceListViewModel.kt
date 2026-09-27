@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.place.ui.home.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.compose.place.PlaceListEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.place.usecase.DeletePlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.GetPlaceListUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PagePlaceHomeUseCase
import io.github.taetae98coding.diary.domain.place.usecase.RestorePlaceUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class PlaceHomePlaceListViewModel(
    getPlaceListUseCase: GetPlaceListUseCase,
    pagePlaceHomeUseCase: PagePlaceHomeUseCase,
    private val deletePlaceUseCase: DeletePlaceUseCase,
    private val restorePlaceUseCase: RestorePlaceUseCase,
) : ViewModel() {
    private val visibleBounds = MutableStateFlow<CoordinateBounds?>(null)

    val sort: StateFlow<ListSort>
        field = MutableStateFlow(ListSort.TITLE)

    val placeListUiState: StateFlow<PlaceHomePlaceListUiState> =
        combine(visibleBounds, sort) { bounds, value -> bounds to value }
            .flatMapLatest { (bounds, value) ->
                if (bounds == null) {
                    flowOf(PlaceHomePlaceListUiState())
                } else {
                    getPlaceListUseCase(parameter = GetPlaceListUseCase.Parameter(bounds = bounds, sort = value))
                        .map { result ->
                            PlaceHomePlaceListUiState(
                                isLoaded = true,
                                placeList = result.getOrDefault(emptyList()),
                            )
                        }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = PlaceHomePlaceListUiState(),
            )

    val placePagingData: Flow<PagingData<Place>> =
        sort
            .flatMapLatest { value -> pagePlaceHomeUseCase(parameter = value) }
            .map { result -> result.getOrElse { PagingData.empty() } }
            .cachedIn(viewModelScope)

    private val _effect = Channel<PlaceListEffect>(Channel.BUFFERED)
    val effect: Flow<PlaceListEffect> = _effect.receiveAsFlow()

    fun updateVisibleBounds(bounds: CoordinateBounds?) {
        visibleBounds.value = bounds
    }

    fun select(sort: ListSort) {
        this.sort.value = sort
    }

    fun delete(id: Uuid) {
        viewModelScope.launch {
            deletePlaceUseCase(parameter = id)
                .onSuccess { _effect.send(PlaceListEffect.Deleted(id = id)) }
        }
    }

    fun restore(id: Uuid) {
        viewModelScope.launch {
            restorePlaceUseCase(parameter = id)
        }
    }
}
