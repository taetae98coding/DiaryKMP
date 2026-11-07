package io.github.taetae98coding.diary.compose.checklist

import kotlin.uuid.Uuid

public sealed interface ChecklistListEffect {
    public data class Deleted(
        val id: Uuid,
    ) : ChecklistListEffect
}
