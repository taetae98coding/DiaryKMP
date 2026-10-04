package io.github.taetae98coding.diary.compose.web

import kotlin.uuid.Uuid

public sealed interface WebListEvent {
    public data class ClickWeb(
        val id: Uuid,
    ) : WebListEvent

    public data class SwipeDelete(
        val id: Uuid,
    ) : WebListEvent
}
