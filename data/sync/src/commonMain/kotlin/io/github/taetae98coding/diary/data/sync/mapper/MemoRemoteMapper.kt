package io.github.taetae98coding.diary.data.sync.mapper

import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoRemoteEntity

internal fun MemoLocalEntity.toRemote(): MemoRemoteEntity =
    MemoRemoteEntity(
        id = id,
        detail = detail.toRemote(),
        primaryTagId = primaryTagId,
        isFinished = isFinished,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

internal fun MemoRemoteEntity.toLocal(): MemoLocalEntity =
    MemoLocalEntity(
        id = id,
        detail = detail.toLocal(),
        primaryTagId = primaryTagId,
        isFinished = isFinished,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
