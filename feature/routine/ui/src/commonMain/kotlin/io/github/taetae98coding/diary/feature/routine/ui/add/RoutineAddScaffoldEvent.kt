package io.github.taetae98coding.diary.feature.routine.ui.add

internal sealed interface RoutineAddScaffoldEvent {
    data object ClickNavigateUp : RoutineAddScaffoldEvent

    data object ClickAdd : RoutineAddScaffoldEvent
}
