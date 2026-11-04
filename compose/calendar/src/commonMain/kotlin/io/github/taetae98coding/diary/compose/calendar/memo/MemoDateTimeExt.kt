package io.github.taetae98coding.diary.compose.calendar.memo

import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import kotlinx.datetime.LocalDateRange

public fun MemoDateTime.toDateRange(): LocalDateRange =
    when (this) {
        is MemoDateTime.AllDay -> dateRange
        is MemoDateTime.DateTime -> start.date..endInclusive.date
    }
