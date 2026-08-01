package io.github.taetae98coding.diary.feature.memo.ui.place

import io.github.taetae98coding.diary.core.model.place.Place

internal data class MemoPlaceInputUiState(
    val isSelectedPlaceLoaded: Boolean = false,
    val selectedPlaceList: List<Place> = emptyList(),
)
