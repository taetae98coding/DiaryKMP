package io.github.taetae98coding.diary.core.mapper.memo

import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoRemoteEntity

public fun Memo.toLocal(): MemoLocalEntity =
    MemoLocalEntity(
        id = id,
        detail = detail.toLocal(),
        primaryTagId = primaryTagId,
        isFinished = isFinished,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

public fun MemoLocalEntity.toDomain(): Memo =
    Memo(
        id = id,
        detail = detail.toDomain(),
        primaryTagId = primaryTagId,
        isFinished = isFinished,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

public fun MemoLocalEntity.toRemote(): MemoRemoteEntity =
    MemoRemoteEntity(
        id = id,
        detail = detail.toRemote(),
        primaryTagId = primaryTagId,
        isFinished = isFinished,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

public fun MemoRemoteEntity.toLocal(): MemoLocalEntity =
    MemoLocalEntity(
        id = id,
        detail = detail.toLocal(),
        primaryTagId = primaryTagId,
        isFinished = isFinished,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
