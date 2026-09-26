package io.github.taetae98coding.diary.core.model.qr

import kotlin.time.Instant
import kotlin.uuid.Uuid

public data class Qr(
    val id: Uuid,
    val detail: QrDetail,
    val isDeleted: Boolean,
    val updatedAt: Instant,
    val createdAt: Instant,
)
