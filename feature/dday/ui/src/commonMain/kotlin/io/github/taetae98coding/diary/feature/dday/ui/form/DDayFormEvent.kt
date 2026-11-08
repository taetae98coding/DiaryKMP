package io.github.taetae98coding.diary.feature.dday.ui.form

import kotlin.uuid.Uuid

internal sealed interface DDayFormEvent {
    data object ClickTagAdd : DDayFormEvent

    data class ClickTag(
        val id: Uuid,
    ) : DDayFormEvent
}
