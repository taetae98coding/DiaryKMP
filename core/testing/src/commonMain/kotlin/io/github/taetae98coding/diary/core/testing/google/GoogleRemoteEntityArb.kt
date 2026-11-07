package io.github.taetae98coding.diary.core.testing.google

import io.github.taetae98coding.diary.core.google.network.api.entity.GooglePlaceDisplayNameRemoteEntity
import io.github.taetae98coding.diary.core.google.network.api.entity.GooglePlaceLocationBiasRemoteEntity
import io.github.taetae98coding.diary.core.google.network.api.entity.GooglePlaceLocationRemoteEntity
import io.github.taetae98coding.diary.core.google.network.api.entity.GooglePlaceRemoteEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.numericDouble
import io.kotest.property.arbitrary.string

public fun Arb.Companion.googlePlaceDisplayNameRemoteEntity(): Arb<GooglePlaceDisplayNameRemoteEntity> =
    arbitrary {
        GooglePlaceDisplayNameRemoteEntity(
            text = Arb.string().bind(),
        )
    }

public fun Arb.Companion.googlePlaceLocationBiasRemoteEntity(): Arb<GooglePlaceLocationBiasRemoteEntity> =
    arbitrary {
        GooglePlaceLocationBiasRemoteEntity(
            latitude = Arb.numericDouble().bind(),
            longitude = Arb.numericDouble().bind(),
            radiusMeters = Arb.numericDouble().bind(),
        )
    }

public fun Arb.Companion.googlePlaceLocationRemoteEntity(): Arb<GooglePlaceLocationRemoteEntity> =
    arbitrary {
        GooglePlaceLocationRemoteEntity(
            latitude = Arb.numericDouble().bind(),
            longitude = Arb.numericDouble().bind(),
        )
    }

public fun Arb.Companion.googlePlaceRemoteEntity(): Arb<GooglePlaceRemoteEntity> =
    arbitrary {
        GooglePlaceRemoteEntity(
            displayName = Arb.googlePlaceDisplayNameRemoteEntity().bind(),
            location = Arb.googlePlaceLocationRemoteEntity().bind(),
            formattedAddress = Arb.string().bind(),
            shortFormattedAddress = Arb.string().bind(),
        )
    }
