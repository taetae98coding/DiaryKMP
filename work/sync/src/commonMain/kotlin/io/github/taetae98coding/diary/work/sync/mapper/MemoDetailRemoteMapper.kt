package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoDetailLocalEntity
import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoDetailRemoteEntity

internal fun MemoDetailLocalEntity.toRemote(): MemoDetailRemoteEntity =
    MemoDetailRemoteEntity(
        title = title,
        description = description,
        color = color,
        isAllDay = isAllDay,
        start = start,
        endInclusive = endInclusive,
    )

internal fun MemoDetailRemoteEntity.toLocal(): MemoDetailLocalEntity =
    MemoDetailLocalEntity(
        title = title,
        description = description,
        color = color,
        isAllDay = isAllDay,
        start = start,
        endInclusive = endInclusive,
    )
