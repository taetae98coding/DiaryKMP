package io.github.taetae98coding.diary.feature.place.ui.search

import io.github.taetae98coding.diary.core.model.place.SearchedPlace

internal sealed interface PlaceSearchEvent {
    data object ClearSearch : PlaceSearchEvent

    data class Search(
        val request: PlaceSearchRequest,
    ) : PlaceSearchEvent

    data class Select(
        val place: SearchedPlace,
    ) : PlaceSearchEvent
}
