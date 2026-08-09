package io.github.taetae98coding.diary.data.location.repository

import io.github.taetae98coding.diary.core.ipnetwork.api.datasource.IpRemoteDataSource
import io.github.taetae98coding.diary.core.location.api.LocationProvider
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.domain.location.repository.LocationRepository
import org.koin.core.annotation.Factory

@Factory
internal class LocationRepositoryImpl(
    private val locationProvider: LocationProvider,
    private val ipRemoteDataSource: IpRemoteDataSource,
) : LocationRepository {
    override suspend fun fetch(): Coordinate =
        locationProvider
            .getCurrentLocation()
            ?.let { location -> Coordinate(latitude = location.latitude, longitude = location.longitude) }
            ?: ipRemoteDataSource.get().let { entity -> Coordinate(latitude = entity.latitude, longitude = entity.longitude) }
}
