package io.github.taetae98coding.diary.core.testing.dday

import io.github.taetae98coding.diary.core.model.dday.CalendarDDay
import io.github.taetae98coding.diary.core.model.dday.CalendarDDayLabel
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.dday.DDayDetail
import io.github.taetae98coding.diary.core.model.dday.DDayOverflowPolicy
import io.github.taetae98coding.diary.library.fixture.MAX_COLLECTION_SIZE
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.string
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus

private const val MAX_REPEAT_DAYS = 10_000
private const val MAX_REPEAT_MONTHS = 1_200
private const val MAX_END_DATE_OFFSET_DAYS = 3_650
private const val MONTHS_PER_YEAR = 12

public fun Arb.Companion.ddayRepeat(): Arb<DateTimeUnit.DateBased> =
    Arb.choice(
        Arb.int(1..MAX_REPEAT_DAYS).map { days -> DateTimeUnit.DayBased(days = days) },
        Arb.int(1..MAX_REPEAT_MONTHS).map { months -> DateTimeUnit.MonthBased(months = months) },
    )

public fun Arb.Companion.ddayDetail(): Arb<DDayDetail> =
    arbitrary {
        val baseDate = Arb.kotlinxLocalDate().bind()

        DDayDetail(
            title = Arb.string().bind(),
            description = Arb.string().bind(),
            color = Arb.long().bind(),
            baseDate = baseDate,
            countsBaseDay = Arb.boolean().bind(),
            repeats = Arb.set(Arb.ddayRepeat(), 0..MAX_COLLECTION_SIZE).bind(),
            endDate =
                Arb
                    .int(0..MAX_END_DATE_OFFSET_DAYS)
                    .map { offset -> baseDate.plus(offset, DateTimeUnit.DAY) }
                    .orNull()
                    .bind(),
            overflowPolicy = Arb.enum<DDayOverflowPolicy>().bind(),
        )
    }

public fun Arb.Companion.dday(): Arb<DDay> =
    arbitrary {
        DDay(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.ddayDetail().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }

public fun Arb.Companion.calendarDDayLabel(): Arb<CalendarDDayLabel> =
    Arb.choice(
        Arb.constant(CalendarDDayLabel.BaseDay),
        Arb.int(1..MAX_REPEAT_DAYS).map { days -> CalendarDDayLabel.Days(days = days) },
        Arb.int(1..MAX_REPEAT_MONTHS).map { months -> CalendarDDayLabel.Months(months = months) },
        Arb.int(1..MAX_REPEAT_MONTHS / MONTHS_PER_YEAR).map { years -> CalendarDDayLabel.Years(years = years) },
    )

public fun Arb.Companion.calendarDDay(): Arb<CalendarDDay> =
    arbitrary {
        CalendarDDay(
            ddayId = Arb.kotlinUuid().bind(),
            title = Arb.string().bind(),
            color = Arb.long().bind(),
            date = Arb.kotlinxLocalDate().bind(),
            label = Arb.calendarDDayLabel().bind(),
        )
    }
