package io.github.taetae98coding.diary.core.testing.place

import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.core.model.place.SearchedPlace
import io.github.taetae98coding.diary.core.testing.location.coordinate
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string

public fun Arb.Companion.place(): Arb<Place> =
    arbitrary {
        Place(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.placeDetail().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }

public fun Arb.Companion.placeDetail(): Arb<PlaceDetail> =
    arbitrary {
        PlaceDetail(
            title = Arb.string().bind(),
            description = Arb.string().bind(),
            color = Arb.long().bind(),
            coordinate = Arb.coordinate().bind(),
            address = Arb.string().bind(),
        )
    }

public fun Arb.Companion.searchedPlace(): Arb<SearchedPlace> =
    arbitrary {
        SearchedPlace(
            id = Arb.kotlinUuid().bind(),
            name = Arb.string().bind(),
            address = Arb.string().bind(),
            coordinate = Arb.coordinate().bind(),
        )
    }
