package io.github.taetae98coding.diary.core.weather.network.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.core.weather.network.api.datasource.WeatherRemoteDataSource
import io.github.taetae98coding.diary.core.weather.network.impl.di.WeatherHttpClient
import io.ktor.client.HttpClient

@DependencyGraph(scope = AppScope::class, excludes = [WeatherNetworkModule::class])
internal interface WeatherNetworkTestGraph {
    val weatherRemoteDataSource: WeatherRemoteDataSource

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides @WeatherHttpClient httpClient: HttpClient,
        ): WeatherNetworkTestGraph
    }
}
