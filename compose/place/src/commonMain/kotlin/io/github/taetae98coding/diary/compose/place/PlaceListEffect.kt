package io.github.taetae98coding.diary.compose.place

import kotlin.uuid.Uuid

public sealed interface PlaceListEffect {
    public data class Deleted(
        val id: Uuid,
    ) : PlaceListEffect
}
