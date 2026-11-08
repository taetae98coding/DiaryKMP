package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayLocalEntity
import io.github.taetae98coding.diary.core.network.api.dday.entity.DDayRemoteEntity

internal fun DDayLocalEntity.toRemote(): DDayRemoteEntity =
    DDayRemoteEntity(
        id = id,
        detail = detail.toRemote(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

internal fun DDayRemoteEntity.toLocal(): DDayLocalEntity =
    DDayLocalEntity(
        id = id,
        detail = detail.toLocal(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
