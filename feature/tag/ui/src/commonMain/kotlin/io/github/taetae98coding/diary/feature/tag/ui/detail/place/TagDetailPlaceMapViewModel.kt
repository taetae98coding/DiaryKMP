package io.github.taetae98coding.diary.feature.tag.ui.detail.place

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.domain.location.usecase.FetchCurrentLocationUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.GetDefaultMapProviderUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class TagDetailPlaceMapViewModel(
    private val fetchCurrentLocationUseCase: FetchCurrentLocationUseCase,
    getDefaultMapProviderUseCase: GetDefaultMapProviderUseCase,
) : ViewModel() {
    private val currentLocationState = MutableStateFlow<CurrentLocationState>(CurrentLocationState.None)

    val uiState: StateFlow<TagDetailPlaceUiState> =
        combine(
            getDefaultMapProviderUseCase(parameter = Unit),
            currentLocationState,
        ) { providerResult, currentLocationState ->
            val provider = providerResult.getOrNull()

            if (provider == null || currentLocationState !is CurrentLocationState.Finished) {
                TagDetailPlaceUiState.Loading
            } else {
                TagDetailPlaceUiState.Loaded(
                    defaultProvider = provider,
                    initialCoordinate = currentLocationState.coordinate,
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TagDetailPlaceUiState.Loading,
        )

    fun fetchCurrentLocation() {
        if (currentLocationState.value != CurrentLocationState.None) return

        currentLocationState.value = CurrentLocationState.InProgress

        viewModelScope.launch {
            val coordinate = fetchCurrentLocationUseCase(parameter = Unit).getOrNull()

            currentLocationState.value = CurrentLocationState.Finished(coordinate = coordinate)
        }
    }
}

private sealed interface CurrentLocationState {
    data object None : CurrentLocationState

    data object InProgress : CurrentLocationState

    data class Finished(
        val coordinate: Coordinate?,
    ) : CurrentLocationState
}
