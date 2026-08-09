package io.github.taetae98coding.diary.feature.place.ui.form

import kotlin.uuid.Uuid

internal sealed interface PlaceFormEvent {
    data object ClickTagAdd : PlaceFormEvent

    data class ClickTag(
        val id: Uuid,
    ) : PlaceFormEvent
}
