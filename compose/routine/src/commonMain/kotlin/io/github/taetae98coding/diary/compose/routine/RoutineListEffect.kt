package io.github.taetae98coding.diary.compose.routine

import kotlin.uuid.Uuid

public sealed interface RoutineListEffect {
    public data class Finished(
        val id: Uuid,
    ) : RoutineListEffect

    public data class Restarted(
        val id: Uuid,
    ) : RoutineListEffect

    public data class Deleted(
        val id: Uuid,
    ) : RoutineListEffect
}
