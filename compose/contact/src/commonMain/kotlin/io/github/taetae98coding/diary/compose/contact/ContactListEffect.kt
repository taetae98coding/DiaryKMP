package io.github.taetae98coding.diary.compose.contact

import kotlin.uuid.Uuid

public sealed interface ContactListEffect {
    public data class Deleted(
        val id: Uuid,
    ) : ContactListEffect
}
