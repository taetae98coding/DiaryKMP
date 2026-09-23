package io.github.taetae98coding.diary.compose.tag.entity

import kotlin.uuid.Uuid

public sealed interface EntityTagPickerEvent {
    public data object ClickAdd : EntityTagPickerEvent

    public data class Add(
        val id: Uuid,
    ) : EntityTagPickerEvent

    public data class Remove(
        val id: Uuid,
    ) : EntityTagPickerEvent

    public data class ChangeQuery(
        val query: String,
    ) : EntityTagPickerEvent
}
