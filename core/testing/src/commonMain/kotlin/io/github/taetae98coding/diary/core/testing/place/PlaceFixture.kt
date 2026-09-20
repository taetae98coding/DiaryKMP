package io.github.taetae98coding.diary.core.testing.place

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.network.api.place.entity.PlaceRemoteEntity

public fun FixtureMonkey.place(isDeleted: Boolean): Place =
    giveMeKotlinBuilder<Place>()
        .setExp(Place::isDeleted, isDeleted)
        .sample()

public fun FixtureMonkey.localPlace(isDeleted: Boolean): PlaceLocalEntity =
    giveMeKotlinBuilder<PlaceLocalEntity>()
        .setExp(PlaceLocalEntity::isDeleted, isDeleted)
        .sample()

public fun FixtureMonkey.remotePlace(isDeleted: Boolean): PlaceRemoteEntity =
    giveMeKotlinBuilder<PlaceRemoteEntity>()
        .setExp(PlaceRemoteEntity::isDeleted, isDeleted)
        .sample()
