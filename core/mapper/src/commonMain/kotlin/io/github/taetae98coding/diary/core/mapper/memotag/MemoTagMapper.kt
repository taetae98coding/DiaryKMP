package io.github.taetae98coding.diary.core.mapper.memotag

import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.network.api.memotag.entity.MemoTagRemoteEntity

public fun MemoTagLocalEntity.toRemote(): MemoTagRemoteEntity =
    MemoTagRemoteEntity(
        memoId = memoId,
        tagId = tagId,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

public fun MemoTagRemoteEntity.toLocal(): MemoTagLocalEntity =
    MemoTagLocalEntity(
        memoId = memoId,
        tagId = tagId,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
