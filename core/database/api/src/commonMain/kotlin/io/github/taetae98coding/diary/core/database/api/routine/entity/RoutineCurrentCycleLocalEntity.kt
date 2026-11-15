package io.github.taetae98coding.diary.core.database.api.routine.entity

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange

public data class RoutineCurrentCycleLocalEntity(
    val today: LocalDate,
    val week: LocalDateRange,
    val month: LocalDateRange,
)
