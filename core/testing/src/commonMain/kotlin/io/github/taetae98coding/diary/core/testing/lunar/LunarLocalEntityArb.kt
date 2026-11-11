package io.github.taetae98coding.diary.core.testing.lunar

import io.github.taetae98coding.diary.core.calendar.database.api.entity.LunarDateLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int

public fun Arb.Companion.lunarDateLocalEntity(): Arb<LunarDateLocalEntity> =
    arbitrary {
        LunarDateLocalEntity(
            solar = Arb.kotlinxLocalDate().bind(),
            solarYear = Arb.int().bind(),
            lunarYear = Arb.int().bind(),
            lunarMonth = Arb.int().bind(),
            lunarDay = Arb.int().bind(),
            isLeapMonth = Arb.boolean().bind(),
        )
    }
