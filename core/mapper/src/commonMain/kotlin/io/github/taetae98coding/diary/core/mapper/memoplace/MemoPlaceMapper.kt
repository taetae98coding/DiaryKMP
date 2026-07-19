package io.github.taetae98coding.diary.core.mapper.memoplace

import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.network.api.memoplace.entity.MemoPlaceRemoteEntity

public fun MemoPlaceLocalEntity.toRemote(): MemoPlaceRemoteEntity =
    MemoPlaceRemoteEntity(
        memoId = memoId,
        placeId = placeId,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

public fun MemoPlaceRemoteEntity.toLocal(): MemoPlaceLocalEntity =
    MemoPlaceLocalEntity(
        memoId = memoId,
        placeId = placeId,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
