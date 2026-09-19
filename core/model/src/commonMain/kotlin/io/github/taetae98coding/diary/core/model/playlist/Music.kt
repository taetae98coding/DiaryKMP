package io.github.taetae98coding.diary.core.model.playlist

import kotlin.time.Instant
import kotlin.uuid.Uuid

public data class Music(
    val id: Uuid,
    val detail: MusicDetail,
    val isDeleted: Boolean,
    val updatedAt: Instant,
    val createdAt: Instant,
)
