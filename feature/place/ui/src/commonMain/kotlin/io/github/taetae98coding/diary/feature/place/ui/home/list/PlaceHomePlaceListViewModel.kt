@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.place.ui.home.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import io.github.taetae98coding.diary.compose.place.PlaceListEffect
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.place.usecase.DeletePlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.GetPlaceListUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PagePlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.RestorePlaceUseCase
import io.github.taetae98coding.diary.feature.core.list.ListItemDeleteActions
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
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
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
internal class PlaceHomePlaceListViewModel(
    getPlaceListUseCase: GetPlaceListUseCase,
    pagePlaceUseCase: PagePlaceUseCase,
    deletePlaceUseCase: DeletePlaceUseCase,
    restorePlaceUseCase: RestorePlaceUseCase,
) : ViewModel() {
    private val visibleBounds = MutableStateFlow<CoordinateBounds?>(null)

    val sortUiState: StateFlow<ListSortUiState>
        field = MutableStateFlow(ListSortUiState(sort = ListSort.TITLE))

    val placeListUiState: StateFlow<PlaceHomePlaceListUiState> =
        combine(visibleBounds, sortUiState) { bounds, (value) -> bounds to value }
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
        sortUiState
            .flatMapLatest { (value) -> pagePlaceUseCase(parameter = PagePlaceUseCase.Parameter(query = "", sort = value)) }
            .map { result -> result.getOrElse { PagingData.empty() } }
            .cachedIn(viewModelScope)

    private val placeActions =
        ListItemDeleteActions<PlaceListEffect>(
            deleteUseCase = deletePlaceUseCase,
            restoreUseCase = restorePlaceUseCase,
            deletedEffect = PlaceListEffect::Deleted,
        )

    val effect: Flow<PlaceListEffect> = placeActions.effect

    fun updateVisibleBounds(bounds: CoordinateBounds?) {
        visibleBounds.value = bounds
    }

    fun select(sort: ListSort) {
        sortUiState.value = ListSortUiState(sort = sort)
    }

    fun delete(id: Uuid) {
        viewModelScope.launch { placeActions.delete(id = id) }
    }

    fun restore(id: Uuid) {
        viewModelScope.launch { placeActions.restore(id = id) }
    }
}
