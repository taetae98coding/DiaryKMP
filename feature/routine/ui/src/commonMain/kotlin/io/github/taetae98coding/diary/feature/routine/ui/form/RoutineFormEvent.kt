package io.github.taetae98coding.diary.feature.routine.ui.form

import kotlin.uuid.Uuid

internal sealed interface RoutineFormEvent {
    data object ClickTagAdd : RoutineFormEvent

    data class ClickTag(
        val id: Uuid,
    ) : RoutineFormEvent
}
