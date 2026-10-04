package io.github.taetae98coding.diary.core.location.impl

import io.github.taetae98coding.diary.core.location.api.Location
import io.github.taetae98coding.diary.core.location.api.LocationProvider
import org.koin.core.annotation.Factory

@Factory
internal class UnsupportedLocationProvider : LocationProvider {
    override suspend fun getCurrentLocation(): Location? = null
}
