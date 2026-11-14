package io.github.taetae98coding.diary.feature.routine.ui.finished

internal sealed interface RoutineFinishedListScaffoldEvent {
    data object ClickNavigateUp : RoutineFinishedListScaffoldEvent

    data object Refresh : RoutineFinishedListScaffoldEvent
}
