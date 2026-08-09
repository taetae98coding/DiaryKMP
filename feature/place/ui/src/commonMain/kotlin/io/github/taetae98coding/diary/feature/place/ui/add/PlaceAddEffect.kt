package io.github.taetae98coding.diary.feature.place.ui.add

import kotlin.uuid.Uuid

internal sealed interface PlaceAddEffect {
    data class AddSucceeded(
        val id: Uuid,
    ) : PlaceAddEffect

    data object TitleBlank : PlaceAddEffect

    data object CoordinateInvalid : PlaceAddEffect
}
