package io.github.taetae98coding.diary.work.appaction.contact

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

// iOS는 생일을 기기 시간대의 Date로 주고받으므로, 생일 날짜는 그날 0시로 바꾸고 입력은 기기 시간대의 날짜로 읽는다.
public fun appActionContactBirthdayEpochMillis(
    date: String,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): Long = LocalDate.parse(date).atStartOfDayIn(timeZone).toEpochMilliseconds()

public fun appActionContactBirthdayArgument(
    epochMillis: Long?,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String? =
    epochMillis?.let { millis ->
        Instant
            .fromEpochMilliseconds(millis)
            .toLocalDateTime(timeZone)
            .date
            .toString()
    }

// iOS는 키를 숫자로 받으므로, 데스크톱 연결과 같은 검사를 거치도록 같은 글 형식으로 바꿔 넘긴다.
public fun appActionContactHeightArgument(heightCm: Double?): String? = heightCm?.toString()
