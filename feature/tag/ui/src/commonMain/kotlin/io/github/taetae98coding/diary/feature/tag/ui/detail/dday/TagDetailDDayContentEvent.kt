package io.github.taetae98coding.diary.feature.tag.ui.detail.dday

import kotlin.uuid.Uuid

internal sealed interface TagDetailDDayContentEvent {
    data class ClickDDay(
        val id: Uuid,
    ) : TagDetailDDayContentEvent

    data object Refresh : TagDetailDDayContentEvent

    data class DeleteDDay(
        val id: Uuid,
    ) : TagDetailDDayContentEvent
}
