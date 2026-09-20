package io.github.taetae98coding.diary.data.sync.mapper

import io.github.taetae98coding.diary.core.database.api.memoweb.entity.MemoWebLocalEntity
import io.github.taetae98coding.diary.core.network.api.memoweb.entity.MemoWebRemoteEntity

internal fun MemoWebLocalEntity.toRemote(): MemoWebRemoteEntity =
    MemoWebRemoteEntity(
        memoId = memoId,
        webId = webId,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

internal fun MemoWebRemoteEntity.toLocal(): MemoWebLocalEntity =
    MemoWebLocalEntity(
        memoId = memoId,
        webId = webId,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
