package io.github.taetae98coding.diary.core.testing.holiday

import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.string
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/** [year]의 임의 날짜에 시작해 [dayCount]일 동안 이어지는 공휴일을 만든다. */
public fun Arb.Companion.holiday(
    year: Int,
    dayCount: Int = 1,
    isHoliday: Boolean? = null,
): Arb<Holiday> =
    arbitrary {
        val date = Arb.kotlinxLocalDate().bind()
        val start = LocalDate(year = year, month = date.month, day = date.day)

        Holiday(
            name = Arb.string().bind(),
            isHoliday = isHoliday ?: Arb.boolean().bind(),
            dateRange = start..start.plus(dayCount - 1, DateTimeUnit.DAY),
        )
    }
