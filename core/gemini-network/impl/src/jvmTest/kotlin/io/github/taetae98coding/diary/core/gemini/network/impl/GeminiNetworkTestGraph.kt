package io.github.taetae98coding.diary.core.gemini.network.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.core.gemini.network.api.datasource.GeminiContentRemoteDataSource
import io.github.taetae98coding.diary.core.gemini.network.api.datasource.GeminiModelRemoteDataSource
import io.github.taetae98coding.diary.core.gemini.network.impl.di.GeminiHttpClient
import io.github.taetae98coding.diary.core.gemini.network.impl.di.GeminiJson
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

@DependencyGraph(scope = AppScope::class, excludes = [GeminiNetworkModule::class])
internal interface GeminiNetworkTestGraph {
    val geminiContentRemoteDataSource: GeminiContentRemoteDataSource
    val geminiModelRemoteDataSource: GeminiModelRemoteDataSource

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides @GeminiHttpClient httpClient: HttpClient,
            @Provides @GeminiJson json: Json,
        ): GeminiNetworkTestGraph
    }
}
