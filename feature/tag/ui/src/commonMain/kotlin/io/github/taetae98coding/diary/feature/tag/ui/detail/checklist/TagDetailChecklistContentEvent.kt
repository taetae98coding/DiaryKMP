package io.github.taetae98coding.diary.feature.tag.ui.detail.checklist

import kotlin.uuid.Uuid

internal sealed interface TagDetailChecklistContentEvent {
    data class ClickChecklist(
        val id: Uuid,
    ) : TagDetailChecklistContentEvent

    data object Refresh : TagDetailChecklistContentEvent

    data class DeleteChecklist(
        val id: Uuid,
    ) : TagDetailChecklistContentEvent
}
