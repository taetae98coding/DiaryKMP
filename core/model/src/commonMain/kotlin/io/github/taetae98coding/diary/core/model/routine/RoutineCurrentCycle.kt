package io.github.taetae98coding.diary.core.model.routine

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange

public data class RoutineCurrentCycle(
    val today: LocalDate,
    val week: LocalDateRange,
    val month: LocalDateRange,
)
