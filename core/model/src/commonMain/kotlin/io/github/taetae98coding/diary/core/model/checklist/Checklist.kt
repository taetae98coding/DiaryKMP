package io.github.taetae98coding.diary.core.model.checklist

import kotlin.time.Instant
import kotlin.uuid.Uuid

public data class Checklist(
    val id: Uuid,
    val detail: ChecklistDetail,
    val isDeleted: Boolean,
    val updatedAt: Instant,
    val createdAt: Instant,
)
