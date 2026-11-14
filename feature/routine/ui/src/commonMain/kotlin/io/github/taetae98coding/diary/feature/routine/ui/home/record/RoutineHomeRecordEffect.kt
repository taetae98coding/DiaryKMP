package io.github.taetae98coding.diary.feature.routine.ui.home.record

import kotlin.uuid.Uuid

internal sealed interface RoutineHomeRecordEffect {
    data class Recorded(
        val recordId: Uuid,
        val record: RoutineHomeRecord,
    ) : RoutineHomeRecordEffect
}
