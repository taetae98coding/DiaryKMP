package io.github.taetae98coding.diary.compose.contact

import kotlin.uuid.Uuid

public sealed interface ContactListEvent {
    public data class ClickContact(
        val id: Uuid,
    ) : ContactListEvent

    public data class SwipeDelete(
        val id: Uuid,
    ) : ContactListEvent
}
