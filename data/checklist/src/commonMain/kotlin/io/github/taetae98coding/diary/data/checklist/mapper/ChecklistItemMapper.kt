package io.github.taetae98coding.diary.data.checklist.mapper

import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemSortLocalEntity
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItem
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItemSort

internal fun ChecklistItem.toLocal(): ChecklistItemLocalEntity =
    ChecklistItemLocalEntity(
        id = id,
        checklistId = checklistId,
        content = content,
        isChecked = isChecked,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

internal fun ChecklistItemLocalEntity.toDomain(): ChecklistItem =
    ChecklistItem(
        id = id,
        checklistId = checklistId,
        content = content,
        isChecked = isChecked,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

internal fun ChecklistItemSort.toLocal(): ChecklistItemSortLocalEntity =
    when (this) {
        ChecklistItemSort.ADDED -> ChecklistItemSortLocalEntity.ADDED
        ChecklistItemSort.UNCHECKED_FIRST -> ChecklistItemSortLocalEntity.UNCHECKED_FIRST
    }
