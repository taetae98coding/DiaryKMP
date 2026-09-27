package io.github.taetae98coding.diary.compose.web

import kotlin.uuid.Uuid

public sealed interface WebListEffect {
    public data class Deleted(
        val id: Uuid,
    ) : WebListEffect
}
