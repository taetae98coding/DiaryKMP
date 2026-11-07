package io.github.taetae98coding.diary.core.testing.holiday

import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.holiday.HolidaySetting
import io.github.taetae98coding.diary.library.fixture.localDateRange
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.string

public fun Arb.Companion.holiday(): Arb<Holiday> =
    arbitrary {
        Holiday(
            name = Arb.string().bind(),
            isHoliday = Arb.boolean().bind(),
            dateRange = Arb.localDateRange().bind(),
        )
    }

public fun Arb.Companion.holidaySetting(): Arb<HolidaySetting> =
    arbitrary {
        HolidaySetting(
            name = Arb.string().bind(),
            isHoliday = Arb.boolean().bind(),
            isVisible = Arb.boolean().bind(),
        )
    }
