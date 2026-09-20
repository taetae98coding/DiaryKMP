package io.github.taetae98coding.diary.domain.memo.usecase

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

internal val DAILY_MEMO_NOTIFICATION_TIME: LocalTime = LocalTime(hour = 8, minute = 0)

internal const val UPCOMING_DAILY_MEMO_NOTIFICATION_DAY_COUNT: Int = 7

internal fun upcomingDailyMemoNotificationDateList(
    now: Instant,
    timeZone: TimeZone,
): List<LocalDate> {
    val localDateTime = now.toLocalDateTime(timeZone)
    val firstDate =
        if (localDateTime.time <= DAILY_MEMO_NOTIFICATION_TIME) {
            localDateTime.date
        } else {
            localDateTime.date.plus(1, DateTimeUnit.DAY)
        }

    return List(size = UPCOMING_DAILY_MEMO_NOTIFICATION_DAY_COUNT) { index -> firstDate.plus(index, DateTimeUnit.DAY) }
}
