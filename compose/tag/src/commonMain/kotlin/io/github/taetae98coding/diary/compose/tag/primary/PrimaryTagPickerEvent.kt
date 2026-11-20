package io.github.taetae98coding.diary.compose.tag.primary

import kotlin.uuid.Uuid

public sealed interface PrimaryTagPickerEvent {
    public data object ClickAdd : PrimaryTagPickerEvent

    public data object UnselectPrimary : PrimaryTagPickerEvent

    public data class Select(
        val id: Uuid,
    ) : PrimaryTagPickerEvent

    public data class Unselect(
        val id: Uuid,
    ) : PrimaryTagPickerEvent

    public data class SelectPrimary(
        val id: Uuid,
    ) : PrimaryTagPickerEvent

    public data class ChangeQuery(
        val query: String,
    ) : PrimaryTagPickerEvent
}
