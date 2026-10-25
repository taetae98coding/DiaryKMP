package io.github.taetae98coding.diary.feature.memo.ui.place

import io.github.taetae98coding.diary.core.model.place.Place

internal data class MemoPlacePickerMapUiState(
    val isLoaded: Boolean = false,
    val placeList: List<Place> = emptyList(),
)
