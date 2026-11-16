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

    // 취소된 앞선 검색이 뒤늦게 끝나면서 지금 검색의 진행 표시를 지우지 않도록, 검색마다 번호를 올려 자기 차례일 때만 지운다.
    private var searchGeneration = 0
    private var inProgressRequest: PlaceSearchRequest? = null

    fun search(request: PlaceSearchRequest) {
        if (request == inProgressRequest) return

        val previousRequest = lastRequest

        lastRequest = request
        searchJob?.cancel()

        if (previousRequest != null && previousRequest.provider != request.provider) {
            uiState.value = PlaceSearchUiState.Idle
        }

        val generation = ++searchGeneration

        inProgressRequest = request
        searchJob =
            viewModelScope.launch {
                try {
                    uiState.value =
                        fetchSearchedPlaceUseCase(parameter = request.toParameter()).fold(
                            onSuccess = { placeList -> PlaceSearchUiState.Content(placeList = placeList) },
                            onFailure = { PlaceSearchUiState.Failed },
                        )
                } finally {
                    if (generation == searchGeneration) inProgressRequest = null
                }
            }
    }

    fun clear() {
        lastRequest = null
        searchGeneration++
        inProgressRequest = null
        searchJob?.cancel()
        searchJob = null
        uiState.value = PlaceSearchUiState.Idle
    }
}
