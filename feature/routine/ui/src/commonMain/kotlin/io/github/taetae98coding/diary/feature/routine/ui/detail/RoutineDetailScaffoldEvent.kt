package io.github.taetae98coding.diary.feature.routine.ui.detail

import kotlin.uuid.Uuid

internal sealed interface RoutineDetailScaffoldEvent {
    data object ClickNavigateUp : RoutineDetailScaffoldEvent

    data object ClickFinish : RoutineDetailScaffoldEvent

    data object ClickRestart : RoutineDetailScaffoldEvent

    data object ClickDelete : RoutineDetailScaffoldEvent

    data object ClickUpdate : RoutineDetailScaffoldEvent

    data object ClickRecordAdd : RoutineDetailScaffoldEvent

    data class ClickRecordRemove(
        val id: Uuid,
    ) : RoutineDetailScaffoldEvent
}
