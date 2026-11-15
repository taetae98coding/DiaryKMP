package io.github.taetae98coding.diary.core.model.routine

import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlin.uuid.Uuid

public data class RoutineRecord(
    val id: Uuid,
    val routineId: Uuid,
    val date: LocalDate,
    val amount: Int,
    val isDeleted: Boolean,
    val updatedAt: Instant,
    val createdAt: Instant,
)
