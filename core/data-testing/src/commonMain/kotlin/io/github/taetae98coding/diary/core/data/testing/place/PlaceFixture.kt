package io.github.taetae98coding.diary.core.data.testing.place

import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.network.api.place.entity.PlaceRemoteEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.map

public fun Arb.Companion.localPlace(isDeleted: Boolean): Arb<PlaceLocalEntity> = Arb.placeLocalEntity().map { place -> place.copy(isDeleted = isDeleted) }

public fun Arb.Companion.remotePlace(isDeleted: Boolean): Arb<PlaceRemoteEntity> = Arb.placeRemoteEntity().map { place -> place.copy(isDeleted = isDeleted) }
