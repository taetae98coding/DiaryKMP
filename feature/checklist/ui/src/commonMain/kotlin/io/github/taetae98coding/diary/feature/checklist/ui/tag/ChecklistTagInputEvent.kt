package io.github.taetae98coding.diary.feature.checklist.ui.tag

import kotlin.uuid.Uuid

internal sealed interface ChecklistTagInputEvent {
    data object ClickTagAdd : ChecklistTagInputEvent

    data class ClickTag(
        val id: Uuid,
    ) : ChecklistTagInputEvent
}
