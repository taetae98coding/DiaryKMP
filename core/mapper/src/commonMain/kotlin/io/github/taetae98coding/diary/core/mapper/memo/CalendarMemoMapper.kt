package io.github.taetae98coding.diary.core.mapper.memo

import io.github.taetae98coding.diary.core.database.api.memo.entity.CalendarMemoLocalEntity
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime

public fun CalendarMemoLocalEntity.toDomain(): CalendarMemo =
    CalendarMemo(
        id = id,
        title = title,
        color = color,
        dateTime = toMemoDateTime(),
    )

private fun CalendarMemoLocalEntity.toMemoDateTime(): MemoDateTime =
    if (isAllDay) {
        MemoDateTime.AllDay(dateRange = start.date..endInclusive.date)
    } else {
        MemoDateTime.DateTime(start = start, endInclusive = endInclusive)
    }
