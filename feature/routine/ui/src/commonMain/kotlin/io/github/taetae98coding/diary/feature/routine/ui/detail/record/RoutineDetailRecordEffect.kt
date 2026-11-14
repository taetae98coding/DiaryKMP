package io.github.taetae98coding.diary.feature.routine.ui.detail.record

internal sealed interface RoutineDetailRecordEffect {
    data object AddSucceeded : RoutineDetailRecordEffect
}
