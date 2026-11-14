package io.github.taetae98coding.diary.data.routine.mapper

import io.github.taetae98coding.diary.core.database.api.routinerecord.entity.RoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord

internal fun RoutineRecord.toLocal(): RoutineRecordLocalEntity =
    RoutineRecordLocalEntity(
        id = id,
        routineId = routineId,
        date = date,
        amount = amount,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

internal fun RoutineRecordLocalEntity.toDomain(): RoutineRecord =
    RoutineRecord(
        id = id,
        routineId = routineId,
        date = date,
        amount = amount,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
