package io.github.taetae98coding.diary.feature.calendar.ui.home.memo

import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import kotlinx.datetime.LocalDateRange

internal fun MemoDateTime.isSingleDayDateTime(): Boolean =
    when (this) {
        is MemoDateTime.AllDay -> false
        is MemoDateTime.DateTime -> start.date == endInclusive.date
    }

internal fun MemoDateTime.toDateRange(): LocalDateRange =
    when (this) {
        is MemoDateTime.AllDay -> dateRange
        is MemoDateTime.DateTime -> start.date..endInclusive.date
    }
