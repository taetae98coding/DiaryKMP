package io.github.taetae98coding.diary.core.mapper.memo

import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoDetailLocalEntity
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoDetailRemoteEntity
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

public fun MemoDetail.toLocal(): MemoDetailLocalEntity =
    MemoDetailLocalEntity(
        title = title,
        description = description,
        color = color,
        isAllDay = dateTime?.let { it is MemoDateTime.AllDay },
        start = dateTime?.toStartLocalDateTime(),
        endInclusive = dateTime?.toEndInclusiveLocalDateTime(),
    )

public fun MemoDetailLocalEntity.toDomain(): MemoDetail =
    MemoDetail(
        title = title,
        description = description,
        color = color,
        dateTime = toMemoDateTime(),
    )

public fun MemoDetailLocalEntity.toRemote(): MemoDetailRemoteEntity =
    MemoDetailRemoteEntity(
        title = title,
        description = description,
        color = color,
        isAllDay = isAllDay,
        start = start,
        endInclusive = endInclusive,
    )

public fun MemoDetailRemoteEntity.toLocal(): MemoDetailLocalEntity =
    MemoDetailLocalEntity(
        title = title,
        description = description,
        color = color,
        isAllDay = isAllDay,
        start = start,
        endInclusive = endInclusive,
    )

private fun MemoDateTime.toStartLocalDateTime(): LocalDateTime =
    when (this) {
        is MemoDateTime.AllDay -> LocalDateTime(date = dateRange.start, time = Midnight)
        is MemoDateTime.DateTime -> start
    }

private fun MemoDateTime.toEndInclusiveLocalDateTime(): LocalDateTime =
    when (this) {
        is MemoDateTime.AllDay -> LocalDateTime(date = dateRange.endInclusive, time = Midnight)
        is MemoDateTime.DateTime -> endInclusive
    }

private fun MemoDetailLocalEntity.toMemoDateTime(): MemoDateTime? {
    val isAllDay = isAllDay
    val start = start
    val endInclusive = endInclusive

    return when {
        isAllDay == null || start == null || endInclusive == null -> null
        isAllDay -> MemoDateTime.AllDay(dateRange = start.date..endInclusive.date)
        else -> MemoDateTime.DateTime(start = start, endInclusive = endInclusive)
    }
}

private val Midnight = LocalTime(hour = 0, minute = 0)
