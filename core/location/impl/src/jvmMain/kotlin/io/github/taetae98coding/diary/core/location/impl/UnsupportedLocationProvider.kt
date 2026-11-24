package io.github.taetae98coding.diary.core.location.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.location.api.Location
import io.github.taetae98coding.diary.core.location.api.LocationProvider

@Inject
@ContributesBinding(AppScope::class)
internal class UnsupportedLocationProvider : LocationProvider {
    override suspend fun getCurrentLocation(): Location? = null
}
