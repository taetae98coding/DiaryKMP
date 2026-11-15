package io.github.taetae98coding.diary.data.core.mapper

import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineDetailLocalEntity
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineDetail

public fun RoutineDetail.toLocal(): RoutineDetailLocalEntity =
    RoutineDetailLocalEntity(
        title = title,
        description = description,
        color = color,
        startDate = startDate,
        cycle = cycle.toLocal(),
        unit = unit,
        goalAmount = goalAmount,
        amountPerRecord = amountPerRecord,
    )

internal fun RoutineDetailLocalEntity.toDomain(): RoutineDetail =
    RoutineDetail(
        title = title,
        description = description,
        color = color,
        startDate = startDate,
        cycle = cycle.toDomain(),
        unit = unit,
        goalAmount = goalAmount,
        amountPerRecord = amountPerRecord,
    )

public fun RoutineCycle.toLocal(): RoutineCycleLocalEntity =
    when (this) {
        RoutineCycle.DAILY -> RoutineCycleLocalEntity.DAILY
        RoutineCycle.WEEKDAYS -> RoutineCycleLocalEntity.WEEKDAYS
        RoutineCycle.WEEKENDS -> RoutineCycleLocalEntity.WEEKENDS
        RoutineCycle.WEEKLY -> RoutineCycleLocalEntity.WEEKLY
        RoutineCycle.MONTHLY -> RoutineCycleLocalEntity.MONTHLY
    }

internal fun RoutineCycleLocalEntity.toDomain(): RoutineCycle =
    when (this) {
        RoutineCycleLocalEntity.DAILY -> RoutineCycle.DAILY
        RoutineCycleLocalEntity.WEEKDAYS -> RoutineCycle.WEEKDAYS
        RoutineCycleLocalEntity.WEEKENDS -> RoutineCycle.WEEKENDS
        RoutineCycleLocalEntity.WEEKLY -> RoutineCycle.WEEKLY
        RoutineCycleLocalEntity.MONTHLY -> RoutineCycle.MONTHLY
    }
