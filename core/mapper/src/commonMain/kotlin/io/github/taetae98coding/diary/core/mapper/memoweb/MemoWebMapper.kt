package io.github.taetae98coding.diary.core.mapper.memoweb

import io.github.taetae98coding.diary.core.database.api.memoweb.entity.MemoWebLocalEntity
import io.github.taetae98coding.diary.core.network.api.memoweb.entity.MemoWebRemoteEntity

public fun MemoWebLocalEntity.toRemote(): MemoWebRemoteEntity =
    MemoWebRemoteEntity(
        memoId = memoId,
        webId = webId,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

public fun MemoWebRemoteEntity.toLocal(): MemoWebLocalEntity =
    MemoWebLocalEntity(
        memoId = memoId,
        webId = webId,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
