package io.github.taetae98coding.diary.compose.memo

import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal fun previewMemo(
    title: String,
    color: Long,
): Memo =
    Memo(
        id = Uuid.random(),
        detail =
            MemoDetail(
                title = title,
                description = "메모 설명",
                color = color,
                dateTime = null,
            ),
        primaryTagId = null,
        isFinished = false,
        isDeleted = false,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST,
    )
