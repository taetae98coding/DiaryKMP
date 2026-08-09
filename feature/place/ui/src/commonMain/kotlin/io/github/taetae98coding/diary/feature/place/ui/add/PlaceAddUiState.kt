package io.github.taetae98coding.diary.feature.place.ui.add

import io.github.taetae98coding.diary.core.model.map.MapProvider

internal data class PlaceAddUiState(
    val isInProgress: Boolean = false,
    val defaultProvider: MapProvider? = null,
)
