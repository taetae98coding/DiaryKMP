package io.github.taetae98coding.diary.core.model.tag

import kotlin.time.Instant
import kotlin.uuid.Uuid

public data class Tag(
    val id: Uuid,
    val detail: TagDetail,
    val isFinished: Boolean,
    val isDeleted: Boolean,
    val updatedAt: Instant,
    val createdAt: Instant,
)
