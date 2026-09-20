package io.github.taetae98coding.diary.data.memo.mapper

import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.model.memo.Memo

internal fun Memo.toLocal(): MemoLocalEntity =
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
