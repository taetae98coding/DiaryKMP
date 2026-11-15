package io.github.taetae98coding.diary.feature.routine.ui.home

import io.github.taetae98coding.diary.feature.routine.ui.home.record.RoutineHomeRecord
import kotlin.uuid.Uuid

internal sealed interface RoutineHomeScaffoldEvent {
    data object ClickAdd : RoutineHomeScaffoldEvent

    data object ClickFilter : RoutineHomeScaffoldEvent

    data object ClickFinishedList : RoutineHomeScaffoldEvent

    data object Refresh : RoutineHomeScaffoldEvent

    data class QuickRecord(
        val record: RoutineHomeRecord,
    ) : RoutineHomeScaffoldEvent

    data class AmountRecord(
        val record: RoutineHomeRecord,
    ) : RoutineHomeScaffoldEvent

    data class ClickCalendarRecord(
        val routineId: Uuid,
    ) : RoutineHomeScaffoldEvent
}
