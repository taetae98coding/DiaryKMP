package io.github.taetae98coding.diary.feature.place.ui.search

import io.github.taetae98coding.diary.core.model.place.SearchedPlace

internal sealed interface PlaceSearchUiState {
    data object Idle : PlaceSearchUiState

    data class Loaded(
        val placeList: List<SearchedPlace> = emptyList(),
    ) : PlaceSearchUiState

    data object Failed : PlaceSearchUiState
}
