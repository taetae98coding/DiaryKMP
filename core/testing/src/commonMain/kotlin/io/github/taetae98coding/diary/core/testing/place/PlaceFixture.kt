package io.github.taetae98coding.diary.core.testing.place

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.core.network.api.place.entity.PlaceRemoteEntity
import kotlin.uuid.Uuid

public fun FixtureMonkey.place(isDeleted: Boolean): Place =
    giveMeKotlinBuilder<Place>()
        .setExp(Place::isDeleted, isDeleted)
        .sample()

public fun FixtureMonkey.place(
    title: String,
    isDeleted: Boolean,
): Place =
    Place(
        id = giveMeOne<Uuid>(),
        detail =
            PlaceDetail(
                title = title,
                description = giveMeOne<String>(),
                color = giveMeOne<Long>(),
                coordinate = coordinateInFormPrecision(),
                address = giveMeOne<String>(),
            ),
        isDeleted = isDeleted,
        updatedAt = giveMeOne(),
        createdAt = giveMeOne(),
    )

public fun FixtureMonkey.localPlace(isDeleted: Boolean): PlaceLocalEntity =
    giveMeKotlinBuilder<PlaceLocalEntity>()
        .setExp(PlaceLocalEntity::isDeleted, isDeleted)
        .sample()

public fun FixtureMonkey.remotePlace(isDeleted: Boolean): PlaceRemoteEntity =
    giveMeKotlinBuilder<PlaceRemoteEntity>()
        .setExp(PlaceRemoteEntity::isDeleted, isDeleted)
        .sample()
