package io.github.taetae98coding.diary.data.sync.mapper

import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceDetailLocalEntity
import io.github.taetae98coding.diary.core.network.api.place.entity.PlaceDetailRemoteEntity

internal fun PlaceDetailLocalEntity.toRemote(): PlaceDetailRemoteEntity =
    PlaceDetailRemoteEntity(
        title = title,
        description = description,
        color = color,
        latitude = latitude,
        longitude = longitude,
        address = address,
    )

internal fun PlaceDetailRemoteEntity.toLocal(): PlaceDetailLocalEntity =
    PlaceDetailLocalEntity(
        title = title,
        description = description,
        color = color,
        latitude = latitude,
        longitude = longitude,
        address = address,
    )
