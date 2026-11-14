package io.github.taetae98coding.diary.feature.tag.ui.routine.finished

internal sealed interface TagRoutineFinishedListScaffoldEvent {
    data object ClickNavigateUp : TagRoutineFinishedListScaffoldEvent

    data object Refresh : TagRoutineFinishedListScaffoldEvent
}
