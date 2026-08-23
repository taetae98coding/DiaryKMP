package io.github.taetae98coding.diary.core.notification.impl

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Instant

internal fun nextDailyMemoNotificationInstant(
    from: Instant,
    time: LocalTime,
    timeZone: TimeZone,
): Instant {
    val date = from.toLocalDateTime(timeZone).date
    val today = date.atTime(time).toInstant(timeZone)

    return if (today >= from) {
        today
    } else {
        date
            .plus(1, DateTimeUnit.DAY)
            .atTime(time)
            .toInstant(timeZone)
    }
}

internal fun dailyMemoNotificationDelay(
    from: Instant,
    time: LocalTime,
    timeZone: TimeZone,
): Duration = nextDailyMemoNotificationInstant(from = from, time = time, timeZone = timeZone) - from
