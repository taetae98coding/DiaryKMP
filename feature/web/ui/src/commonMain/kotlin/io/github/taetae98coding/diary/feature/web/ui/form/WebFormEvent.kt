package io.github.taetae98coding.diary.feature.web.ui.form

import kotlin.uuid.Uuid

internal sealed interface WebFormEvent {
    data object ClickTagAdd : WebFormEvent

    data class ClickTag(
        val id: Uuid,
    ) : WebFormEvent
}
