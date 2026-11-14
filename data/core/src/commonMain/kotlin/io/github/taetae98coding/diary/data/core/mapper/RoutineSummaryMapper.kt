package io.github.taetae98coding.diary.data.core.mapper

import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCurrentCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineSummaryLocalEntity
import io.github.taetae98coding.diary.core.model.routine.RoutineCurrentCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary

public fun RoutineSummaryLocalEntity.toDomain(): RoutineSummary =
    RoutineSummary(
        routine = routine.toDomain(),
        cycleAmount = cycleAmount,
    )

public fun RoutineCurrentCycle.toLocal(): RoutineCurrentCycleLocalEntity =
    RoutineCurrentCycleLocalEntity(
        today = today,
        week = week,
        month = month,
    )
