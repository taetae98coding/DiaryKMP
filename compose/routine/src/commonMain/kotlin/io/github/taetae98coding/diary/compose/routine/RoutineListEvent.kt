package io.github.taetae98coding.diary.compose.routine

import kotlin.uuid.Uuid

public sealed interface RoutineListEvent {
    public data class ClickRoutine(
        val id: Uuid,
    ) : RoutineListEvent

    public data class SwipeFinish(
        val id: Uuid,
    ) : RoutineListEvent

    public data class SwipeRestart(
        val id: Uuid,
    ) : RoutineListEvent

    public data class SwipeDelete(
        val id: Uuid,
    ) : RoutineListEvent
}
