package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineCycleLocalEntity
import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineDetailLocalEntity
import io.github.taetae98coding.diary.core.network.api.routine.entity.RoutineCycleRemoteEntity
import io.github.taetae98coding.diary.core.network.api.routine.entity.RoutineDetailRemoteEntity

internal fun RoutineDetailLocalEntity.toRemote(): RoutineDetailRemoteEntity =
    RoutineDetailRemoteEntity(
        title = title,
        description = description,
        color = color,
        startDate = startDate,
        cycle = cycle.toRemote(),
        unit = unit,
        goalAmount = goalAmount,
        amountPerRecord = amountPerRecord,
    )

internal fun RoutineDetailRemoteEntity.toLocal(): RoutineDetailLocalEntity =
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

internal fun RoutineCycleLocalEntity.toRemote(): RoutineCycleRemoteEntity =
    when (this) {
        RoutineCycleLocalEntity.DAILY -> RoutineCycleRemoteEntity.DAILY
        RoutineCycleLocalEntity.WEEKDAYS -> RoutineCycleRemoteEntity.WEEKDAYS
        RoutineCycleLocalEntity.WEEKENDS -> RoutineCycleRemoteEntity.WEEKENDS
        RoutineCycleLocalEntity.WEEKLY -> RoutineCycleRemoteEntity.WEEKLY
        RoutineCycleLocalEntity.MONTHLY -> RoutineCycleRemoteEntity.MONTHLY
    }

internal fun RoutineCycleRemoteEntity.toLocal(): RoutineCycleLocalEntity =
    when (this) {
        RoutineCycleRemoteEntity.DAILY -> RoutineCycleLocalEntity.DAILY
        RoutineCycleRemoteEntity.WEEKDAYS -> RoutineCycleLocalEntity.WEEKDAYS
        RoutineCycleRemoteEntity.WEEKENDS -> RoutineCycleLocalEntity.WEEKENDS
        RoutineCycleRemoteEntity.WEEKLY -> RoutineCycleLocalEntity.WEEKLY
        RoutineCycleRemoteEntity.MONTHLY -> RoutineCycleLocalEntity.MONTHLY
    }
