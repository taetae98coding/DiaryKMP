package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.memocontact.entity.MemoContactLocalEntity
import io.github.taetae98coding.diary.core.network.api.memocontact.entity.MemoContactRemoteEntity

internal fun MemoContactLocalEntity.toRemote(): MemoContactRemoteEntity =
    MemoContactRemoteEntity(
        memoId = memoId,
        contactId = contactId,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

internal fun MemoContactRemoteEntity.toLocal(): MemoContactLocalEntity =
    MemoContactLocalEntity(
        memoId = memoId,
        contactId = contactId,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
