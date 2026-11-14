package io.github.taetae98coding.diary.feature.routine.ui.detail

internal sealed interface RoutineDetailEffect {
    data object UpdateSucceeded : RoutineDetailEffect

    data object GoalAmountInvalid : RoutineDetailEffect

    data object AmountPerRecordInvalid : RoutineDetailEffect

    data object DeleteSucceeded : RoutineDetailEffect
}
