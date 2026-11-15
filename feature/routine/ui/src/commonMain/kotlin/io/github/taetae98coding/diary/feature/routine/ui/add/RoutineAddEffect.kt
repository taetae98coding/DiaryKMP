package io.github.taetae98coding.diary.feature.routine.ui.add

internal sealed interface RoutineAddEffect {
    data object AddSucceeded : RoutineAddEffect

    data object TitleBlank : RoutineAddEffect

    data object GoalAmountInvalid : RoutineAddEffect

    data object AmountPerRecordInvalid : RoutineAddEffect
}
