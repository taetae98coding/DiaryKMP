package io.github.taetae98coding.diary.feature.tag.ui.link

import kotlin.uuid.Uuid

internal sealed interface TagLinkPickerEvent {
    data object ClickAdd : TagLinkPickerEvent

    data class Link(
        val id: Uuid,
    ) : TagLinkPickerEvent

    data class Unlink(
        val id: Uuid,
    ) : TagLinkPickerEvent

    data class ChangeQuery(
        val query: String,
    ) : TagLinkPickerEvent
}
