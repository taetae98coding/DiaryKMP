package io.github.taetae98coding.diary.feature.place.ui.detail

internal sealed interface PlaceDetailEffect {
    data object UpdateSucceeded : PlaceDetailEffect

    data object CoordinateInvalid : PlaceDetailEffect

    data object DeleteSucceeded : PlaceDetailEffect
}
