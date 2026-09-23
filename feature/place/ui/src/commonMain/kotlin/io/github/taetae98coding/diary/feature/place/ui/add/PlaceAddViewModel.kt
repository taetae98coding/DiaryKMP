package io.github.taetae98coding.diary.feature.place.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.domain.place.exception.PlaceCoordinateInvalidException
import io.github.taetae98coding.diary.domain.place.exception.PlaceTitleBlankException
import io.github.taetae98coding.diary.domain.place.usecase.AddPlaceUseCase
import io.github.taetae98coding.diary.domain.setting.usecase.GetDefaultMapProviderUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class PlaceAddViewModel(
    private val addPlaceUseCase: AddPlaceUseCase,
    getDefaultMapProviderUseCase: GetDefaultMapProviderUseCase,
) : ViewModel() {
    private val isInProgress = MutableStateFlow(false)

    val uiState: StateFlow<PlaceAddUiState> =
        combine(
            isInProgress,
            getDefaultMapProviderUseCase(parameter = Unit),
        ) { isInProgress, providerResult ->
            PlaceAddUiState(
                isInProgress = isInProgress,
                defaultProvider = providerResult.getOrNull(),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = PlaceAddUiState(),
        )

    private val _effect = Channel<PlaceAddEffect>(Channel.BUFFERED)
    val effect: Flow<PlaceAddEffect> = _effect.receiveAsFlow()

    fun add(
        detail: PlaceDetail,
        tagIdSet: Set<Uuid>,
    ) {
        if (isInProgress.value) return

        viewModelScope.launch {
            isInProgress.value = true
            try {
                addPlaceUseCase(parameter = AddPlaceUseCase.Parameter(detail = detail, tagIdSet = tagIdSet))
                    .onSuccess { id -> _effect.send(PlaceAddEffect.AddSucceeded(id = id)) }
                    .onFailure { throwable ->
                        when (throwable) {
                            is PlaceTitleBlankException -> _effect.send(PlaceAddEffect.TitleBlank)
                            is PlaceCoordinateInvalidException -> _effect.send(PlaceAddEffect.CoordinateInvalid)
                        }
                    }
            } finally {
                isInProgress.value = false
            }
        }
    }
}
