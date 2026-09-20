package io.github.taetae98coding.diary.data.place.mapper

import io.github.taetae98coding.diary.core.google.network.api.entity.GooglePlaceLocationBias
import io.github.taetae98coding.diary.core.model.location.CoordinateCircle

internal fun CoordinateCircle.toRemote(): GooglePlaceLocationBias =
    GooglePlaceLocationBias(
        latitude = center.latitude,
        longitude = center.longitude,
        radiusMeters = radiusMeters,
    )
