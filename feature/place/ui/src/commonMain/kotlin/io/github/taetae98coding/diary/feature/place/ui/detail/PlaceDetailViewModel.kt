package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.domain.place.exception.PlaceCoordinateInvalidException
import io.github.taetae98coding.diary.domain.place.usecase.DeletePlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.FindPlaceUseCase
import io.github.taetae98coding.diary.domain.place.usecase.UpdatePlaceUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.GetDefaultMapProviderUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class PlaceDetailViewModel(
    @InjectedParam private val id: Uuid,
    private val updatePlaceUseCase: UpdatePlaceUseCase,
    private val deletePlaceUseCase: DeletePlaceUseCase,
    findPlaceUseCase: FindPlaceUseCase,
    getDefaultMapProviderUseCase: GetDefaultMapProviderUseCase,
) : ViewModel() {
    private val isUpdateInProgress = MutableStateFlow(false)
    private val isDeleteInProgress = MutableStateFlow(false)

    val uiState: StateFlow<PlaceDetailUiState> =
        combine(
            findPlaceUseCase(parameter = id),
            getDefaultMapProviderUseCase(parameter = Unit),
            isUpdateInProgress,
            isDeleteInProgress,
        ) { placeResult, providerResult, isUpdateInProgress, isDeleteInProgress ->
            placeResult.getOrNull()?.let { place ->
                PlaceDetailUiState.Content(
                    id = place.id,
                    detail = place.detail,
                    defaultProvider = providerResult.getOrNull(),
                    isUpdateInProgress = isUpdateInProgress,
                    isDeleteInProgress = isDeleteInProgress,
                )
            } ?: PlaceDetailUiState.Loading
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = PlaceDetailUiState.Loading,
        )

    private val _effect = Channel<PlaceDetailEffect>(Channel.BUFFERED)
    val effect: Flow<PlaceDetailEffect> = _effect.receiveAsFlow()

    fun update(detail: PlaceDetail) {
        if (isUpdateInProgress.value) return

        viewModelScope.launch {
            isUpdateInProgress.value = true
            try {
                updatePlaceUseCase(parameter = UpdatePlaceUseCase.Parameter(id = id, detail = detail))
                    .onSuccess { _effect.send(PlaceDetailEffect.UpdateSucceeded) }
                    .onFailure { throwable ->
                        when (throwable) {
                            is PlaceCoordinateInvalidException -> _effect.send(PlaceDetailEffect.CoordinateInvalid)
                        }
                    }
            } finally {
                isUpdateInProgress.value = false
            }
        }
    }

    fun delete() {
        if (isDeleteInProgress.value) return

        viewModelScope.launch {
            isDeleteInProgress.value = true
            try {
                deletePlaceUseCase(parameter = id)
                    .onSuccess { _effect.send(PlaceDetailEffect.DeleteSucceeded) }
            } finally {
                isDeleteInProgress.value = false
            }
        }
    }
}
