@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.place.usecase.FetchSearchedPlaceUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class PlaceSearchViewModel(
    private val fetchSearchedPlaceUseCase: FetchSearchedPlaceUseCase,
) : ViewModel() {
    private val request = MutableStateFlow<PlaceSearchRequest?>(null)

    val uiState: StateFlow<PlaceSearchUiState> =
        request
            .mapLatest { request ->
                if (request == null) {
                    PlaceSearchUiState.Idle
                } else {
                    fetchSearchedPlaceUseCase(parameter = request.toParameter()).fold(
                        onSuccess = { placeList -> PlaceSearchUiState.Loaded(placeList = placeList) },
                        onFailure = { PlaceSearchUiState.Failed },
                    )
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                initialValue = PlaceSearchUiState.Idle,
            )

    fun search(request: PlaceSearchRequest) {
        this.request.value = request
    }

    fun clear() {
        request.value = null
    }
}
