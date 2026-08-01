package io.github.taetae98coding.diary.feature.memo.ui.place

import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.map.MapProvider

internal sealed interface MemoPlaceMapUiState {
    data object Loading : MemoPlaceMapUiState

    data class Loaded(
        val provider: MapProvider,
        val currentCoordinate: Coordinate?,
    ) : MemoPlaceMapUiState
}
