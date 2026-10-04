package io.github.taetae98coding.diary.compose.place

import kotlin.uuid.Uuid

public sealed interface PlaceListEvent {
    public data class ClickPlace(
        val id: Uuid,
    ) : PlaceListEvent

    public data class SwipeDelete(
        val id: Uuid,
    ) : PlaceListEvent
}
