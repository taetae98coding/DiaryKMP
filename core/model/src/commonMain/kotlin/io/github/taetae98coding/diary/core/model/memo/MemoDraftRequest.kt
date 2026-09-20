package io.github.taetae98coding.diary.core.model.memo

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone

public data class MemoDraftRequest(
    val prompt: String,
    val title: String,
    val description: String,
    val dateTime: MemoDateTime?,
    val now: LocalDateTime,
    val timeZone: TimeZone,
)
