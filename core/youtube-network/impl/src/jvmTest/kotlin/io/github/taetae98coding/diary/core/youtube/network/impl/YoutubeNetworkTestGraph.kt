package io.github.taetae98coding.diary.core.youtube.network.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.core.youtube.network.api.datasource.YoutubeVideoRemoteDataSource
import io.github.taetae98coding.diary.core.youtube.network.impl.di.YoutubeHttpClient
import io.ktor.client.HttpClient

@DependencyGraph(scope = AppScope::class, excludes = [YoutubeNetworkModule::class])
internal interface YoutubeNetworkTestGraph {
    val youtubeVideoRemoteDataSource: YoutubeVideoRemoteDataSource

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides @YoutubeHttpClient httpClient: HttpClient,
        ): YoutubeNetworkTestGraph
    }
}
