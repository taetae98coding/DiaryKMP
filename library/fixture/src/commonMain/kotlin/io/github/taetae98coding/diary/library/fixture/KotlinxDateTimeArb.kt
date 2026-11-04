package io.github.taetae98coding.diary.library.fixture

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus

private const val MIN_YEAR = 1900
private const val MAX_YEAR = 2100
private const val MONTHS_PER_YEAR = 12
private const val MAX_DAY_OF_MONTH = 28
private const val MAX_RANGE_DAY_COUNT = 30
private const val MAX_HOUR = 23
private const val MAX_MINUTE = 59

// Kotest의 localDate·localDateTime·localTime은 java.time 타입을 만들어 공통 코드에서 쓸 수 없다.

public fun Arb.Companion.kotlinxLocalDate(): Arb<LocalDate> =
    arbitrary {
        LocalDate(
            year = Arb.int(MIN_YEAR..MAX_YEAR).bind(),
            month = Arb.int(1..MONTHS_PER_YEAR).bind(),
            day = Arb.int(1..MAX_DAY_OF_MONTH).bind(),
        )
    }

public fun Arb.Companion.kotlinxLocalTime(): Arb<LocalTime> =
    arbitrary {
        LocalTime(
            hour = Arb.int(0..MAX_HOUR).bind(),
            minute = Arb.int(0..MAX_MINUTE).bind(),
            second = Arb.int(0..MAX_MINUTE).bind(),
        )
    }

public fun Arb.Companion.kotlinxLocalDateTime(): Arb<LocalDateTime> =
    arbitrary {
        LocalDateTime(date = Arb.kotlinxLocalDate().bind(), time = Arb.kotlinxLocalTime().bind())
    }

public fun Arb.Companion.localDateRange(): Arb<LocalDateRange> =
    arbitrary {
        val start = Arb.kotlinxLocalDate().bind()

        start..start.plus(Arb.int(0..MAX_RANGE_DAY_COUNT).bind(), DateTimeUnit.DAY)
    }
