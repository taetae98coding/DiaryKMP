package io.github.taetae98coding.diary.core.testing.memo

import kotlin.uuid.Uuid

public data class MemoCase(
    val isFinished: Boolean,
    val isDeleted: Boolean,
    val primaryTagId: Uuid?,
)
