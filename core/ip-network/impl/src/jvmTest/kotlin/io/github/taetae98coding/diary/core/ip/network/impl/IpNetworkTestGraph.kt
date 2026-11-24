package io.github.taetae98coding.diary.core.ip.network.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.core.ip.network.api.datasource.IpRemoteDataSource
import io.github.taetae98coding.diary.core.ip.network.impl.di.IpHttpClient
import io.ktor.client.HttpClient

@DependencyGraph(scope = AppScope::class, excludes = [IpNetworkModule::class])
internal interface IpNetworkTestGraph {
    val ipRemoteDataSource: IpRemoteDataSource

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides @IpHttpClient httpClient: HttpClient,
        ): IpNetworkTestGraph
    }
}
