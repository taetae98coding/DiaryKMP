package io.github.taetae98coding.diary.data.location.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.ip.network.api.datasource.IpRemoteDataSource
import io.github.taetae98coding.diary.core.location.api.LocationProvider
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.domain.location.repository.LocationRepository

@Inject
@ContributesBinding(AppScope::class)
internal class LocationRepositoryImpl(
    private val locationProvider: LocationProvider,
    private val ipRemoteDataSource: IpRemoteDataSource,
) : LocationRepository {
    override suspend fun fetch(): Coordinate =
        locationProvider
            .getCurrentLocation()
            ?.let { location -> Coordinate(latitude = location.latitude, longitude = location.longitude) }
            ?: ipRemoteDataSource.read().let { entity -> Coordinate(latitude = entity.latitude, longitude = entity.longitude) }
}
