package io.github.taetae98coding.diary.core.model.checklist

import kotlin.time.Instant
import kotlin.uuid.Uuid

public data class ChecklistItem(
    val id: Uuid,
    val checklistId: Uuid,
    val content: String,
    val isChecked: Boolean,
    val isDeleted: Boolean,
    val updatedAt: Instant,
    val createdAt: Instant,
)
