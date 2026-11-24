package io.github.taetae98coding.diary.core.web.network.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.core.web.network.api.datasource.WebPageRemoteDataSource
import io.github.taetae98coding.diary.core.web.network.impl.di.WebPageHttpClient
import io.ktor.client.HttpClient

@DependencyGraph(scope = AppScope::class, excludes = [WebNetworkModule::class])
internal interface WebNetworkTestGraph {
    val webPageRemoteDataSource: WebPageRemoteDataSource

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides @WebPageHttpClient httpClient: HttpClient,
        ): WebNetworkTestGraph
    }
}
