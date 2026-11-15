package io.github.taetae98coding.diary.core.model.routine

import kotlin.time.Instant
import kotlin.uuid.Uuid

public data class Routine(
    val id: Uuid,
    val detail: RoutineDetail,
    val isFinished: Boolean,
    val isDeleted: Boolean,
    val updatedAt: Instant,
    val createdAt: Instant,
)
