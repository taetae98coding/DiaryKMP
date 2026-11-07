package io.github.taetae98coding.diary.feature.calendar.ui.home.memo

import io.github.taetae98coding.diary.core.model.memo.MemoDateTime

internal fun MemoDateTime.isSingleDayDateTime(): Boolean =
    when (this) {
        is MemoDateTime.AllDay -> false
        is MemoDateTime.DateTime -> start.date == endInclusive.date
    }
