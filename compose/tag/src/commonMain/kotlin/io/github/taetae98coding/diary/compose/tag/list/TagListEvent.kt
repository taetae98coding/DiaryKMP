package io.github.taetae98coding.diary.compose.tag.list

import kotlin.uuid.Uuid

public sealed interface TagListEvent {
    public data class ClickTag(
        val id: Uuid,
    ) : TagListEvent

    public data class SwipeFinish(
        val id: Uuid,
    ) : TagListEvent

    public data class SwipeRestart(
        val id: Uuid,
    ) : TagListEvent

    public data class SwipeDelete(
        val id: Uuid,
    ) : TagListEvent
}
