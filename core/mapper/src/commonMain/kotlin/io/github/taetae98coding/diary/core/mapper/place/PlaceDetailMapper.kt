package io.github.taetae98coding.diary.core.mapper.place

import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceDetailLocalEntity
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.core.network.api.place.entity.PlaceDetailRemoteEntity

public fun PlaceDetail.toLocal(): PlaceDetailLocalEntity =
    PlaceDetailLocalEntity(
        title = title,
        description = description,
        color = color,
        latitude = coordinate.latitude,
        longitude = coordinate.longitude,
        address = address,
    )

public fun PlaceDetailLocalEntity.toDomain(): PlaceDetail =
    PlaceDetail(
        title = title,
        description = description,
        color = color,
        coordinate =
            Coordinate(
                latitude = latitude,
                longitude = longitude,
            ),
        address = address,
    )

public fun PlaceDetailLocalEntity.toRemote(): PlaceDetailRemoteEntity =
    PlaceDetailRemoteEntity(
        title = title,
        description = description,
        color = color,
        latitude = latitude,
        longitude = longitude,
        address = address,
    )

public fun PlaceDetailRemoteEntity.toLocal(): PlaceDetailLocalEntity =
    PlaceDetailLocalEntity(
        title = title,
        description = description,
        color = color,
        latitude = latitude,
        longitude = longitude,
        address = address,
    )
