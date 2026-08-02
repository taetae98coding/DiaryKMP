package io.github.taetae98coding.diary.feature.tag.ui.detail

import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.map.MapProvider

internal sealed interface TagDetailPlaceUiState {
    data object Loading : TagDetailPlaceUiState

    data class Loaded(
        val defaultProvider: MapProvider,
        val initialCoordinate: Coordinate? = null,
    ) : TagDetailPlaceUiState
}
