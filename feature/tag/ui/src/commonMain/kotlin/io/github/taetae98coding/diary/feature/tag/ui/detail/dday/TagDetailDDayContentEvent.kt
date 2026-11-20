package io.github.taetae98coding.diary.feature.tag.ui.detail.dday

internal sealed interface TagDetailDDayContentEvent {
    data object Refresh : TagDetailDDayContentEvent
}
