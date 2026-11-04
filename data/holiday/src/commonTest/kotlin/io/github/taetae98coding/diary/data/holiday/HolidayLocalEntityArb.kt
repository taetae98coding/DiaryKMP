package io.github.taetae98coding.diary.data.holiday

import io.github.taetae98coding.diary.core.calendar.database.api.entity.HolidayCountryLocalEntity
import io.github.taetae98coding.diary.core.calendar.database.api.entity.HolidayLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string

internal fun Arb.Companion.holidayLocalEntity(): Arb<HolidayLocalEntity> =
    arbitrary {
        HolidayLocalEntity(
            country = Arb.enum<HolidayCountryLocalEntity>().bind(),
            year = Arb.int().bind(),
            name = Arb.string().bind(),
            isHoliday = Arb.boolean().bind(),
            start = Arb.kotlinxLocalDate().bind(),
            endInclusive = Arb.kotlinxLocalDate().bind(),
        )
    }
