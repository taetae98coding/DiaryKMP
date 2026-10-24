package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.network.api.checklistitem.entity.ChecklistItemRemoteEntity

internal fun ChecklistItemLocalEntity.toRemote(): ChecklistItemRemoteEntity =
    ChecklistItemRemoteEntity(
        id = id,
        checklistId = checklistId,
        content = content,
        isChecked = isChecked,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

internal fun ChecklistItemRemoteEntity.toLocal(): ChecklistItemLocalEntity =
    ChecklistItemLocalEntity(
        id = id,
        checklistId = checklistId,
        content = content,
        isChecked = isChecked,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )
