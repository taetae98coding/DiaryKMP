package io.github.taetae98coding.diary.feature.tag.ui.detail.checklist

internal sealed interface TagDetailChecklistContentEvent {
    data object Refresh : TagDetailChecklistContentEvent
}
