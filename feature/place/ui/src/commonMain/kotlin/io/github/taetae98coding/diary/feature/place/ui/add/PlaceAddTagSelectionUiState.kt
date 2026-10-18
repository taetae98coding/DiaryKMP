package io.github.taetae98coding.diary.feature.place.ui.add

import kotlin.uuid.Uuid

internal data class PlaceAddTagSelectionUiState(
    val tagIdSet: Set<Uuid> = emptySet(),
)
