package io.github.taetae98coding.diary.compose.checklist

import io.github.taetae98coding.diary.core.model.checklist.Checklist
import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import kotlin.time.Instant
import kotlin.uuid.Uuid

public fun previewChecklistSummary(
    title: String,
    checkedCount: Int = 3,
    totalCount: Int = 5,
): ChecklistSummary =
    ChecklistSummary(
        checklist =
            Checklist(
                id = Uuid.random(),
                detail = ChecklistDetail(title = title, description = ""),
                isDeleted = false,
                updatedAt = Instant.DISTANT_PAST,
                createdAt = Instant.DISTANT_PAST,
            ),
        checkedCount = checkedCount,
        totalCount = totalCount,
    )
