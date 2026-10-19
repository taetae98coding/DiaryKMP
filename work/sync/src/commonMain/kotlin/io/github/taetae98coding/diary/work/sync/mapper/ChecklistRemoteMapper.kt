package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import io.github.taetae98coding.diary.core.network.api.checklist.entity.ChecklistRemoteEntity

internal fun ChecklistLocalEntity.toRemote(): ChecklistRemoteEntity =
    ChecklistRemoteEntity(
        id = id,
        detail = detail.toRemote(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

internal fun ChecklistRemoteEntity.toLocal(): ChecklistLocalEntity =
    ChecklistLocalEntity(
        id = id,
        detail = detail.toLocal(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
