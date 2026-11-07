package io.github.taetae98coding.diary.core.testing.location

import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.numericDouble

public fun Arb.Companion.coordinate(): Arb<Coordinate> =
    arbitrary {
        Coordinate(
            latitude = Arb.numericDouble().bind(),
            longitude = Arb.numericDouble().bind(),
        )
    }

public fun Arb.Companion.coordinateBounds(): Arb<CoordinateBounds> =
    arbitrary {
        CoordinateBounds(
            south = Arb.numericDouble().bind(),
            north = Arb.numericDouble().bind(),
            west = Arb.numericDouble().bind(),
            east = Arb.numericDouble().bind(),
        )
    }
