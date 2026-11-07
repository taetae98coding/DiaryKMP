package io.github.taetae98coding.diary.core.testing.lunar

import io.github.taetae98coding.diary.core.model.lunar.LunarDate
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int

public fun Arb.Companion.lunarDate(): Arb<LunarDate> =
    arbitrary {
        LunarDate(
            solar = Arb.kotlinxLocalDate().bind(),
            year = Arb.int().bind(),
            month = Arb.int().bind(),
            day = Arb.int().bind(),
            isLeapMonth = Arb.boolean().bind(),
        )
    }
