package io.github.taetae98coding.diary.core.testing.contact

import io.github.taetae98coding.diary.core.model.contact.LunarContactBirthday
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.string

public fun Arb.Companion.lunarContactBirthday(): Arb<LunarContactBirthday> =
    arbitrary {
        LunarContactBirthday(
            contactId = Arb.kotlinUuid().bind(),
            name = Arb.string().bind(),
            nameQualifier = Arb.string().bind(),
            birthday = Arb.kotlinxLocalDate().bind(),
        )
    }
