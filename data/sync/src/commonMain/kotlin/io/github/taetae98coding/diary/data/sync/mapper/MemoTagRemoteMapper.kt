package io.github.taetae98coding.diary.data.sync.mapper

import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.network.api.memotag.entity.MemoTagRemoteEntity

internal fun MemoTagLocalEntity.toRemote(): MemoTagRemoteEntity =
    MemoTagRemoteEntity(
        memoId = memoId,
        tagId = tagId,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

internal fun MemoTagRemoteEntity.toLocal(): MemoTagLocalEntity =
    MemoTagLocalEntity(
        memoId = memoId,
        tagId = tagId,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
