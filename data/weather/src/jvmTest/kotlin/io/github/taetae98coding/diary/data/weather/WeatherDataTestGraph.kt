package io.github.taetae98coding.diary.data.weather

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.core.ip.network.api.datasource.IpRemoteDataSource
import io.github.taetae98coding.diary.core.location.api.LocationProvider
import io.github.taetae98coding.diary.core.weather.network.api.datasource.WeatherRemoteDataSource
import io.github.taetae98coding.diary.domain.weather.usecase.FetchCurrentWeatherUseCase
import kotlin.time.Clock

@DependencyGraph(scope = AppScope::class)
internal interface WeatherDataTestGraph {
    val fetchCurrentWeatherUseCase: FetchCurrentWeatherUseCase

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides locationProvider: LocationProvider,
            @Provides ipRemoteDataSource: IpRemoteDataSource,
            @Provides weatherRemoteDataSource: WeatherRemoteDataSource,
            @Provides clock: Clock,
        ): WeatherDataTestGraph
    }
}
