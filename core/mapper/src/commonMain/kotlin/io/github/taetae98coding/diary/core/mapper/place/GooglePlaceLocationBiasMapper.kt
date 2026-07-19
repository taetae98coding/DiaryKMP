package io.github.taetae98coding.diary.core.mapper.place

import io.github.taetae98coding.diary.core.google.network.api.entity.GooglePlaceLocationBias
import io.github.taetae98coding.diary.core.model.location.CoordinateCircle

public fun CoordinateCircle.toRemote(): GooglePlaceLocationBias =
    GooglePlaceLocationBias(
        latitude = center.latitude,
        longitude = center.longitude,
        radiusMeters = radiusMeters,
    )
