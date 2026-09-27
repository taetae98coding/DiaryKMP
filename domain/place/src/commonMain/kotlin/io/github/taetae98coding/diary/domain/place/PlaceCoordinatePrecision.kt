package io.github.taetae98coding.diary.domain.place

import io.github.taetae98coding.diary.core.model.location.Coordinate
import kotlin.math.pow
import kotlin.math.round

public const val PLACE_COORDINATE_FRACTION_DIGITS: Int = 6

public val PLACE_COORDINATE_SCALE: Long = 10.0.pow(PLACE_COORDINATE_FRACTION_DIGITS).toLong()

public fun Coordinate.toPlacePrecision(): Coordinate =
    Coordinate(
        latitude = latitude.toPlacePrecision(),
        longitude = longitude.toPlacePrecision(),
    )

private fun Double.toPlacePrecision(): Double = if (isFinite()) round(this * PLACE_COORDINATE_SCALE) / PLACE_COORDINATE_SCALE else this
