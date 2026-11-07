package io.github.taetae98coding.diary.core.testing.place

import io.github.taetae98coding.diary.core.network.api.place.entity.PlaceDetailRemoteEntity
import io.github.taetae98coding.diary.core.network.api.place.entity.PlaceRemoteEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.numericDouble
import io.kotest.property.arbitrary.string

public fun Arb.Companion.placeDetailRemoteEntity(): Arb<PlaceDetailRemoteEntity> =
    arbitrary {
        PlaceDetailRemoteEntity(
            title = Arb.string().bind(),
            description = Arb.string().bind(),
            color = Arb.long().bind(),
            latitude = Arb.numericDouble().bind(),
            longitude = Arb.numericDouble().bind(),
            address = Arb.string().bind(),
        )
    }

public fun Arb.Companion.placeRemoteEntity(): Arb<PlaceRemoteEntity> =
    arbitrary {
        PlaceRemoteEntity(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.placeDetailRemoteEntity().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
