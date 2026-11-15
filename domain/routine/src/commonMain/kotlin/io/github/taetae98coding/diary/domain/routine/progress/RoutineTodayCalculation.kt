package io.github.taetae98coding.diary.domain.routine.progress

import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import kotlinx.datetime.DayOfWeek

private val WeekendDayOfWeekSet: Set<DayOfWeek> = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

public fun RoutineCycle.isDoneOn(dayOfWeek: DayOfWeek): Boolean =
    when (this) {
        RoutineCycle.DAILY, RoutineCycle.WEEKLY, RoutineCycle.MONTHLY -> true
        RoutineCycle.WEEKDAYS -> dayOfWeek !in WeekendDayOfWeekSet
        RoutineCycle.WEEKENDS -> dayOfWeek in WeekendDayOfWeekSet
    }
