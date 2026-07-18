package io.github.taetae98coding.diary.core.model.memo

import kotlin.time.Instant
import kotlin.uuid.Uuid

public data class Memo(
    val id: Uuid,
    val detail: MemoDetail,
    val primaryTagId: Uuid?,
    val isFinished: Boolean,
    val isDeleted: Boolean,
    val updatedAt: Instant,
    val createdAt: Instant,
)
