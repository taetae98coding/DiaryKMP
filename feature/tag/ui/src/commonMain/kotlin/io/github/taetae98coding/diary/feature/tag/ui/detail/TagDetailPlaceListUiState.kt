package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.runtime.Immutable
import io.github.taetae98coding.diary.core.model.place.Place

@Immutable
internal data class TagDetailPlaceListUiState(
    val isLoaded: Boolean = false,
    val placeList: List<Place> = emptyList(),
) {
    val isEmpty: Boolean = isLoaded && placeList.isEmpty()
}
