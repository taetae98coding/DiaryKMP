package io.github.taetae98coding.diary.core.model.memo

import kotlin.uuid.Uuid

public data class CalendarMemo(
    val id: Uuid,
    val title: String,
    val color: Long,
    val dateTime: MemoDateTime,
)
