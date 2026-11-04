package io.github.taetae98coding.diary.core.testing.place

import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.core.network.api.place.entity.PlaceRemoteEntity
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string

public fun Arb.Companion.place(isDeleted: Boolean): Arb<Place> = Arb.place().map { place -> place.copy(isDeleted = isDeleted) }

public fun Arb.Companion.place(
    title: String,
    isDeleted: Boolean,
): Arb<Place> =
    arbitrary {
        Place(
            id = Arb.kotlinUuid().bind(),
            detail =
                PlaceDetail(
                    title = title,
                    description = Arb.string().bind(),
                    color = Arb.long().bind(),
                    coordinate = Arb.coordinateInFormPrecision().bind(),
                    address = Arb.string().bind(),
                ),
            isDeleted = isDeleted,
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }

public fun Arb.Companion.localPlace(isDeleted: Boolean): Arb<PlaceLocalEntity> = Arb.placeLocalEntity().map { place -> place.copy(isDeleted = isDeleted) }

public fun Arb.Companion.remotePlace(isDeleted: Boolean): Arb<PlaceRemoteEntity> = Arb.placeRemoteEntity().map { place -> place.copy(isDeleted = isDeleted) }
