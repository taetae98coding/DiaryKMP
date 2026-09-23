package io.github.taetae98coding.diary.feature.place.ui.home.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.domain.location.usecase.FetchCurrentLocationUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.GetDefaultMapProviderUseCase
import io.github.taetae98coding.diary.feature.place.ui.home.PlaceHomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class PlaceHomeMapViewModel(
    private val fetchCurrentLocationUseCase: FetchCurrentLocationUseCase,
    getDefaultMapProviderUseCase: GetDefaultMapProviderUseCase,
) : ViewModel() {
    private val currentLocationState = MutableStateFlow<CurrentLocationState>(CurrentLocationState.None)

    val uiState: StateFlow<PlaceHomeUiState> =
        combine(
            getDefaultMapProviderUseCase(parameter = Unit),
            currentLocationState,
        ) { providerResult, currentLocationState ->
            val provider = providerResult.getOrNull()

            if (provider == null || currentLocationState !is CurrentLocationState.Finished) {
                PlaceHomeUiState.Loading
            } else {
                PlaceHomeUiState.Loaded(
                    defaultProvider = provider,
                    initialCoordinate = currentLocationState.coordinate,
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlaceHomeUiState.Loading,
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
