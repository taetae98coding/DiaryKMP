@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.github.taetae98coding.diary.domain.place.usecase.GetPlaceListUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class MemoPlacePickerMapViewModel(
    getPlaceListUseCase: GetPlaceListUseCase,
) : ViewModel() {
    private val visibleBounds = MutableStateFlow<CoordinateBounds?>(null)

    val uiState: StateFlow<MemoPlacePickerMapUiState> =
        visibleBounds
            .flatMapLatest { bounds ->
                if (bounds == null) {
                    flowOf(MemoPlacePickerMapUiState())
                } else {
                    getPlaceListUseCase(parameter = GetPlaceListUseCase.Parameter(bounds = bounds, sort = ListSort.TITLE))
                        .map { result ->
                            MemoPlacePickerMapUiState(
                                isLoaded = true,
                                placeList = result.getOrDefault(emptyList()),
                            )
                        }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = MemoPlacePickerMapUiState(),
            )

    fun updateVisibleBounds(bounds: CoordinateBounds?) {
        visibleBounds.value = bounds
    }
}
