package io.github.taetae98coding.diary.core.testing.holiday

import io.github.taetae98coding.diary.core.calendar.database.api.entity.HolidayCountryLocalEntity
import io.github.taetae98coding.diary.core.calendar.database.api.entity.HolidayLocalEntity
import io.github.taetae98coding.diary.library.fixture.localDateRange
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string

public fun Arb.Companion.holidayLocalEntity(): Arb<HolidayLocalEntity> =
    arbitrary {
        val dateRange = Arb.localDateRange().bind()

        HolidayLocalEntity(
            country = Arb.enum<HolidayCountryLocalEntity>().bind(),
            year = Arb.int().bind(),
            name = Arb.string().bind(),
            isHoliday = Arb.boolean().bind(),
            start = dateRange.start,
            endInclusive = dateRange.endInclusive,
        )
    }
