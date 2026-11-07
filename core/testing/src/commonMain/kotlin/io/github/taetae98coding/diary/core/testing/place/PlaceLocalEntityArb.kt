package io.github.taetae98coding.diary.core.testing.place

import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.numericDouble
import io.kotest.property.arbitrary.string

public fun Arb.Companion.placeDetailLocalEntity(): Arb<PlaceDetailLocalEntity> =
    arbitrary {
        PlaceDetailLocalEntity(
            title = Arb.string().bind(),
            description = Arb.string().bind(),
            color = Arb.long().bind(),
            latitude = Arb.numericDouble().bind(),
            longitude = Arb.numericDouble().bind(),
            address = Arb.string().bind(),
        )
    }

public fun Arb.Companion.placeLocalEntity(): Arb<PlaceLocalEntity> =
    arbitrary {
        PlaceLocalEntity(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.placeDetailLocalEntity().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
