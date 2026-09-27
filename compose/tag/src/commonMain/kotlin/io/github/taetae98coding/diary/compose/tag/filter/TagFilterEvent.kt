package io.github.taetae98coding.diary.compose.tag.filter

import kotlin.uuid.Uuid

public sealed interface TagFilterEvent {
    public data class Select(
        val id: Uuid,
    ) : TagFilterEvent

    public data class Unselect(
        val id: Uuid,
    ) : TagFilterEvent

    public data object UnselectAll : TagFilterEvent

    public data object ClickAdd : TagFilterEvent
}
