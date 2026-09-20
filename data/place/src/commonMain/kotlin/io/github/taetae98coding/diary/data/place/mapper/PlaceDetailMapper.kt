package io.github.taetae98coding.diary.data.place.mapper

import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceDetailLocalEntity
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.PlaceDetail

internal fun PlaceDetail.toLocal(): PlaceDetailLocalEntity =
    PlaceDetailLocalEntity(
        title = title,
        description = description,
        color = color,
        latitude = coordinate.latitude,
        longitude = coordinate.longitude,
        address = address,
    )

internal fun PlaceDetailLocalEntity.toDomain(): PlaceDetail =
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
