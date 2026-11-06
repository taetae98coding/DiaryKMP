package io.github.taetae98coding.diary.compose.checklist

import kotlin.uuid.Uuid

public sealed interface ChecklistListEvent {
    public data class ClickChecklist(
        val id: Uuid,
    ) : ChecklistListEvent

    public data class SwipeDelete(
        val id: Uuid,
    ) : ChecklistListEvent
}
