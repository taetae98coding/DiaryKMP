@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.place.usecase.FetchSearchedPlaceUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class PlaceSearchViewModel(
    private val fetchSearchedPlaceUseCase: FetchSearchedPlaceUseCase,
) : ViewModel() {
    private val request = MutableStateFlow<PlaceSearchRequest?>(null)

    val uiState: StateFlow<PlaceSearchUiState> =
        request
            .runningFold(initial = PlaceSearchRequestChange()) { change, request ->
                PlaceSearchRequestChange(previous = change.current, current = request)
            }.transformLatest { change ->
                val request = change.current
                if (request == null) {
                    emit(PlaceSearchUiState.Idle)
                    return@transformLatest
                }

                if (change.previous != null && change.previous.provider != request.provider) {
                    emit(PlaceSearchUiState.Idle)
                }

                val uiState =
                    fetchSearchedPlaceUseCase(parameter = request.toParameter()).fold(
                        onSuccess = { placeList -> PlaceSearchUiState.Loaded(placeList = placeList) },
                        onFailure = { PlaceSearchUiState.Failed },
                    )
                emit(uiState)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileUiSubscribed,
                initialValue = PlaceSearchUiState.Idle,
            )

    fun search(request: PlaceSearchRequest) {
        this.request.value = request
    }

    fun clear() {
        request.value = null
    }
}

private data class PlaceSearchRequestChange(
    val previous: PlaceSearchRequest? = null,
    val current: PlaceSearchRequest? = null,
)
