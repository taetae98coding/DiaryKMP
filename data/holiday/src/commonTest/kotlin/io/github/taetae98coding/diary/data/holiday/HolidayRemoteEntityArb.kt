package io.github.taetae98coding.diary.data.holiday

import io.github.taetae98coding.diary.core.calendar.network.api.entity.HolidayRemoteEntity
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.string

internal fun Arb.Companion.holidayRemoteEntity(): Arb<HolidayRemoteEntity> =
    arbitrary {
        HolidayRemoteEntity(
            name = Arb.string().bind(),
            isHoliday = Arb.boolean().bind(),
            start = Arb.kotlinxLocalDate().bind(),
            endInclusive = Arb.kotlinxLocalDate().bind(),
        )
    }
