package io.github.taetae98coding.diary.feature.contact.ui.form

import kotlin.uuid.Uuid

internal sealed interface ContactFormEvent {
    data object ClickTagAdd : ContactFormEvent

    data class ClickTag(
        val id: Uuid,
    ) : ContactFormEvent
}
