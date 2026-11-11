package io.github.taetae98coding.diary.core.testing.holiday

import io.github.taetae98coding.diary.core.calendar.network.api.entity.HolidayRemoteEntity
import io.github.taetae98coding.diary.library.fixture.localDateRange
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.string

public fun Arb.Companion.holidayRemoteEntity(): Arb<HolidayRemoteEntity> =
    arbitrary {
        val dateRange = Arb.localDateRange().bind()

        HolidayRemoteEntity(
            name = Arb.string().bind(),
            isHoliday = Arb.boolean().bind(),
            start = dateRange.start,
            endInclusive = dateRange.endInclusive,
        )
    }
