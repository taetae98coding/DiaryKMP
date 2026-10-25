package io.github.taetae98coding.diary.data.checklist.mapper

import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistSummaryLocalEntity
import io.github.taetae98coding.diary.core.model.checklist.Checklist
import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary

internal fun ChecklistDetail.toLocal(): ChecklistDetailLocalEntity =
    ChecklistDetailLocalEntity(
        title = title,
        description = description,
    )

internal fun ChecklistDetailLocalEntity.toDomain(): ChecklistDetail =
    ChecklistDetail(
        title = title,
        description = description,
    )

internal fun Checklist.toLocal(): ChecklistLocalEntity =
    ChecklistLocalEntity(
        id = id,
        detail = detail.toLocal(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

internal fun ChecklistLocalEntity.toDomain(): Checklist =
    Checklist(
        id = id,
        detail = detail.toDomain(),
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

internal fun ChecklistSummaryLocalEntity.toDomain(): ChecklistSummary =
    ChecklistSummary(
        checklist = checklist.toDomain(),
        checkedCount = checkedCount,
        totalCount = totalCount,
    )
