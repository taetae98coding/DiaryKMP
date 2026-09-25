package io.github.taetae98coding.diary.core.model.file

import kotlin.time.Instant
import kotlin.uuid.Uuid

public data class DiaryFile(
    val id: Uuid,
    val name: String,
    val mimeType: String,
    val size: Long,
    val createdAt: Instant,
)
