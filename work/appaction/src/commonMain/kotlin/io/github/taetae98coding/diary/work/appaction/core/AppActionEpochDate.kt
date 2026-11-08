package io.github.taetae98coding.diary.work.appaction.core

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

// iOS는 날짜를 기기 시간대의 Date로 주고받으므로, 날짜 입력은 그 시각이 속한 기기 시간대의 날짜로, 날짜 결과는 그날 0시로 바꾼다.
public fun appActionDate(
    epochMillis: Long,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String =
    Instant
        .fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(timeZone)
        .date
        .toString()

public fun appActionDateEpochMillis(
    date: String,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): Long = LocalDate.parse(date).atStartOfDayIn(timeZone).toEpochMilliseconds()
