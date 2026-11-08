package io.github.taetae98coding.diary.compose.dday

import kotlin.uuid.Uuid

public sealed interface DDayListEvent {
    public data class ClickDDay(
        val id: Uuid,
    ) : DDayListEvent

    public data class SwipeDelete(
        val id: Uuid,
    ) : DDayListEvent
}
