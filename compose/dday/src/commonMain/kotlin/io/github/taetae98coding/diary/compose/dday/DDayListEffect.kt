package io.github.taetae98coding.diary.compose.dday

import kotlin.uuid.Uuid

public sealed interface DDayListEffect {
    public data class Deleted(
        val id: Uuid,
    ) : DDayListEffect
}
