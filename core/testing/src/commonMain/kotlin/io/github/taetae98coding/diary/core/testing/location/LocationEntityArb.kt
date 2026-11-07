package io.github.taetae98coding.diary.core.testing.location

import io.github.taetae98coding.diary.core.location.api.Location
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.numericDouble

public fun Arb.Companion.location(): Arb<Location> =
    arbitrary {
        Location(
            latitude = Arb.numericDouble().bind(),
            longitude = Arb.numericDouble().bind(),
        )
    }
