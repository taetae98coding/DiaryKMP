package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.place.usecase.FetchSearchedPlaceUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class PlaceSearchViewModel(
    private val fetchSearchedPlaceUseCase: FetchSearchedPlaceUseCase,
) : ViewModel() {
    // 결과는 검색한 순간에 한 번 받는 값이므로 구독이 잠시 끊겨도 다시 조회하지 않도록 ViewModel이 들고 있는다.
    val uiState: StateFlow<PlaceSearchUiState>
        field = MutableStateFlow<PlaceSearchUiState>(PlaceSearchUiState.Idle)

    private var lastRequest: PlaceSearchRequest? = null
    private var searchJob: Job? = null

    fun search(request: PlaceSearchRequest) {
        val previousRequest = lastRequest

        lastRequest = request
        searchJob?.cancel()

        if (previousRequest != null && previousRequest.provider != request.provider) {
            uiState.value = PlaceSearchUiState.Idle
        }

        searchJob =
            viewModelScope.launch {
                uiState.value =
                    fetchSearchedPlaceUseCase(parameter = request.toParameter()).fold(
                        onSuccess = { placeList -> PlaceSearchUiState.Loaded(placeList = placeList) },
                        onFailure = { PlaceSearchUiState.Failed },
                    )
            }
    }

    fun clear() {
        lastRequest = null
        searchJob?.cancel()
        searchJob = null
        uiState.value = PlaceSearchUiState.Idle
    }
}
