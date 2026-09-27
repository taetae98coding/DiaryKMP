package io.github.taetae98coding.diary.feature.place.ui.detail

import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import kotlin.uuid.Uuid

internal sealed interface PlaceDetailUiState {
    data object Loading : PlaceDetailUiState

    data class Content(
        val id: Uuid,
        val detail: PlaceDetail,
        val defaultProvider: MapProvider? = null,
        val isUpdateInProgress: Boolean = false,
        val isDeleteInProgress: Boolean = false,
    ) : PlaceDetailUiState {
        val isMapDisplayed: Boolean
            get() = defaultProvider != null
    }
}
