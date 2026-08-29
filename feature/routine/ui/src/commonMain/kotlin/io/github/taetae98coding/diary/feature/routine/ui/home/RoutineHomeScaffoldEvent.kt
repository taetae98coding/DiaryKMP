package io.github.taetae98coding.diary.feature.routine.ui.home

internal sealed interface RoutineHomeScaffoldEvent {
    data object ClickAdd : RoutineHomeScaffoldEvent

    data object Refresh : RoutineHomeScaffoldEvent
}
