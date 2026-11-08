package io.github.taetae98coding.diary.work.appaction.memo

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

public data class AppActionMemoEpochDateTime(
    val allDay: Boolean,
    val startEpochMillis: Long,
    val endInclusiveEpochMillis: Long,
)

// iOS는 기간을 기기 시간대의 Date로 주고받으므로, 날짜만 쓰는 기간은 그날 0시로, 시간이 있는 기간은 그 시각으로 바꾼다.
public fun AppActionMemoDateTime.toEpochDateTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): AppActionMemoEpochDateTime =
    AppActionMemoEpochDateTime(
        allDay = allDay,
        startEpochMillis = start.toEpochMillis(allDay = allDay, timeZone = timeZone),
        endInclusiveEpochMillis = endInclusive.toEpochMillis(allDay = allDay, timeZone = timeZone),
    )

// 셋 다 비어 있으면 기간을 정하지 않은 것이고, 일부만 있으면 데스크톱 연결과 같은 실패를 내도록 그대로 넘긴다.
// 앱의 기간은 분 단위로 정하므로 시간이 있는 기간은 초 아래를 버린다.
public fun appActionMemoDateTimeInput(
    allDay: Boolean?,
    startEpochMillis: Long?,
    endInclusiveEpochMillis: Long?,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): AppActionMemoDateTimeInput? {
    if (allDay == null && startEpochMillis == null && endInclusiveEpochMillis == null) return null

    return AppActionMemoDateTimeInput(
        allDay = allDay,
        start = startEpochMillis?.toDateTimeArgument(allDay = allDay == true, timeZone = timeZone),
        endInclusive = endInclusiveEpochMillis?.toDateTimeArgument(allDay = allDay == true, timeZone = timeZone),
    )
}

private fun String.toEpochMillis(
    allDay: Boolean,
    timeZone: TimeZone,
): Long {
    val instant = if (allDay) LocalDate.parse(this).atStartOfDayIn(timeZone) else LocalDateTime.parse(this).toInstant(timeZone)

    return instant.toEpochMilliseconds()
}

private fun Long.toDateTimeArgument(
    allDay: Boolean,
    timeZone: TimeZone,
): String {
    val dateTime = Instant.fromEpochMilliseconds(this).toLocalDateTime(timeZone)

    return if (allDay) {
        dateTime.date.toString()
    } else {
        LocalDateTime(date = dateTime.date, time = LocalTime(hour = dateTime.hour, minute = dateTime.minute)).toString()
    }
}
