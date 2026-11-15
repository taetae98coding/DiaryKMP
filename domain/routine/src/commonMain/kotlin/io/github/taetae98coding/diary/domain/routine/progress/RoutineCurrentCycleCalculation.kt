package io.github.taetae98coding.diary.domain.routine.progress

import io.github.taetae98coding.diary.core.model.routine.RoutineCurrentCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.library.kotlinx.datetime.DAYS_PER_WEEK
import io.github.taetae98coding.diary.library.kotlinx.datetime.sundayOfWeek
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.minus
import kotlinx.datetime.plus

private const val FIRST_DAY_OF_MONTH: Int = 1

public fun routineCurrentCycle(today: LocalDate): RoutineCurrentCycle {
    val weekStart = today.sundayOfWeek()
    val monthStart = LocalDate(year = today.year, month = today.month, day = FIRST_DAY_OF_MONTH)

    return RoutineCurrentCycle(
        today = today,
        week = LocalDateRange(start = weekStart, endInclusive = weekStart.plus(DAYS_PER_WEEK - 1, DateTimeUnit.DAY)),
        month = LocalDateRange(start = monthStart, endInclusive = monthStart.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)),
    )
}

public fun RoutineCurrentCycle.range(cycle: RoutineCycle): LocalDateRange =
    when (cycle) {
        RoutineCycle.DAILY, RoutineCycle.WEEKDAYS, RoutineCycle.WEEKENDS -> LocalDateRange(start = today, endInclusive = today)
        RoutineCycle.WEEKLY -> week
        RoutineCycle.MONTHLY -> month
    }
