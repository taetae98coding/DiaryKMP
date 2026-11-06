package io.github.taetae98coding.diary.feature.checklist.ui

import io.github.taetae98coding.diary.core.model.checklist.Checklist
import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import io.github.taetae98coding.diary.core.model.checklist.ChecklistItem
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal fun previewChecklist(title: String): Checklist =
    Checklist(
        id = Uuid.random(),
        detail = ChecklistDetail(title = title, description = ""),
        isDeleted = false,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST,
    )

internal fun previewChecklistItem(
    content: String,
    isChecked: Boolean = false,
): ChecklistItem =
    ChecklistItem(
        id = Uuid.random(),
        checklistId = Uuid.NIL,
        content = content,
        isChecked = isChecked,
        isDeleted = false,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST,
    )
