package io.github.taetae98coding.diary.feature.place.ui.home

import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.map.MapProvider
import kotlin.uuid.Uuid

internal sealed interface PlaceHomeUiState {
    data object Loading : PlaceHomeUiState

    data class Content(
        val defaultProvider: MapProvider,
        val initialCoordinate: Coordinate?,
        val currentLocationFetchId: Uuid,
    ) : PlaceHomeUiState
}
