package io.github.taetae98coding.diary.core.naver.network.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.core.naver.network.api.datasource.NaverPlaceRemoteDataSource
import io.github.taetae98coding.diary.core.naver.network.impl.di.NaverHttpClient
import io.ktor.client.HttpClient

@DependencyGraph(scope = AppScope::class, excludes = [NaverNetworkModule::class])
internal interface NaverNetworkTestGraph {
    val naverPlaceRemoteDataSource: NaverPlaceRemoteDataSource

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides @NaverHttpClient httpClient: HttpClient,
        ): NaverNetworkTestGraph
    }
}
