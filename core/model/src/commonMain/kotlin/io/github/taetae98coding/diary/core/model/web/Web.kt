package io.github.taetae98coding.diary.core.model.web

import kotlin.time.Instant
import kotlin.uuid.Uuid

public data class Web(
    val id: Uuid,
    val detail: WebDetail,
    val isDeleted: Boolean,
    val updatedAt: Instant,
    val createdAt: Instant,
)
