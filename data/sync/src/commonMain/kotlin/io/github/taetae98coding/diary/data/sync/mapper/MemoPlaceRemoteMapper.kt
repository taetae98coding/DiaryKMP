package io.github.taetae98coding.diary.data.sync.mapper

import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.network.api.memoplace.entity.MemoPlaceRemoteEntity

internal fun MemoPlaceLocalEntity.toRemote(): MemoPlaceRemoteEntity =
    MemoPlaceRemoteEntity(
        memoId = memoId,
        placeId = placeId,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

internal fun MemoPlaceRemoteEntity.toLocal(): MemoPlaceLocalEntity =
    MemoPlaceLocalEntity(
        memoId = memoId,
        placeId = placeId,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
