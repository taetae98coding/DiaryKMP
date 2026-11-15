package io.github.taetae98coding.diary.feature.tag.ui.detail.routine

internal sealed interface TagDetailRoutineContentEvent {
    data object Refresh : TagDetailRoutineContentEvent

    data object ClickFinishedList : TagDetailRoutineContentEvent
}
