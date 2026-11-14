package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.routine.entity.RoutineLocalEntity
import io.github.taetae98coding.diary.core.network.api.routine.entity.RoutineRemoteEntity

internal fun RoutineLocalEntity.toRemote(): RoutineRemoteEntity =
    RoutineRemoteEntity(
        id = id,
        detail = detail.toRemote(),
        isFinished = isFinished,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

internal fun RoutineRemoteEntity.toLocal(): RoutineLocalEntity =
    RoutineLocalEntity(
        id = id,
        detail = detail.toLocal(),
        isFinished = isFinished,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
