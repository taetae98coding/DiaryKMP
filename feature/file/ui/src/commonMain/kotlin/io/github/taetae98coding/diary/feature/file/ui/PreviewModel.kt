package io.github.taetae98coding.diary.feature.file.ui

import io.github.taetae98coding.diary.core.model.file.DiaryFile
import kotlin.time.Instant
import kotlin.uuid.Uuid

private const val PREVIEW_CREATED_AT_EPOCH_SECONDS = 1_790_000_000L

internal fun previewDiaryFile(
    title: String,
    name: String,
    size: Long,
): DiaryFile =
    DiaryFile(
        id = Uuid.random(),
        name = name,
        title = title,
        description = "",
        mimeType = "application/octet-stream",
        size = size,
        createdAt = Instant.fromEpochSeconds(PREVIEW_CREATED_AT_EPOCH_SECONDS),
    )
