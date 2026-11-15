package io.github.taetae98coding.diary.core.location.impl

import android.location.Location
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.numericDouble

private const val MAX_LATITUDE = 90.0
private const val MAX_LONGITUDE = 180.0

internal fun Arb.Companion.androidLocation(): Arb<Location> =
    arbitrary {
        Location("test").apply {
            latitude = Arb.numericDouble(min = -MAX_LATITUDE, max = MAX_LATITUDE).bind()
            longitude = Arb.numericDouble(min = -MAX_LONGITUDE, max = MAX_LONGITUDE).bind()
        }
    }
