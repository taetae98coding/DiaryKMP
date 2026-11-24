package io.github.taetae98coding.diary.core.google.network.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.core.google.network.api.datasource.GooglePlaceRemoteDataSource
import io.github.taetae98coding.diary.core.google.network.impl.di.GoogleHttpClient
import io.ktor.client.HttpClient

@DependencyGraph(scope = AppScope::class, excludes = [GoogleNetworkModule::class])
internal interface GoogleNetworkTestGraph {
    val googlePlaceRemoteDataSource: GooglePlaceRemoteDataSource

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides @GoogleHttpClient httpClient: HttpClient,
        ): GoogleNetworkTestGraph
    }
}
