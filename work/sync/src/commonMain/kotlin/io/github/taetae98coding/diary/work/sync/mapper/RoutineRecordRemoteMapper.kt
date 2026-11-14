package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.routinerecord.entity.RoutineRecordLocalEntity
import io.github.taetae98coding.diary.core.network.api.routinerecord.entity.RoutineRecordRemoteEntity

internal fun RoutineRecordLocalEntity.toRemote(): RoutineRecordRemoteEntity =
    RoutineRecordRemoteEntity(
        id = id,
        routineId = routineId,
        date = date,
        amount = amount,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

internal fun RoutineRecordRemoteEntity.toLocal(): RoutineRecordLocalEntity =
    RoutineRecordLocalEntity(
        id = id,
        routineId = routineId,
        date = date,
        amount = amount,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
