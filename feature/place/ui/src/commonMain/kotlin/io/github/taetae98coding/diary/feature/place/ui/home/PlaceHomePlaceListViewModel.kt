@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.place.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.place.usecase.GetPlaceListUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PagePlaceHomeUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class PlaceHomePlaceListViewModel(
    getPlaceListUseCase: GetPlaceListUseCase,
    pagePlaceHomeUseCase: PagePlaceHomeUseCase,
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
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = PlaceHomePlaceListUiState(),
            )

    val placePagingData: Flow<PagingData<Place>> =
        sort
            .flatMapLatest { value -> pagePlaceHomeUseCase(parameter = value) }
            .map { result -> result.getOrElse { PagingData.empty() } }
            .cachedIn(viewModelScope)

    fun updateVisibleBounds(bounds: CoordinateBounds?) {
        visibleBounds.value = bounds
    }

    fun select(sort: ListSort) {
        this.sort.value = sort
    }
}
