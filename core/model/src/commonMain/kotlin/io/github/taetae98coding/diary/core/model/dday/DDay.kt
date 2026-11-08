package io.github.taetae98coding.diary.core.model.dday

import kotlin.time.Instant
import kotlin.uuid.Uuid

public data class DDay(
    val id: Uuid,
    val detail: DDayDetail,
    val isDeleted: Boolean,
    val updatedAt: Instant,
    val createdAt: Instant,
)
