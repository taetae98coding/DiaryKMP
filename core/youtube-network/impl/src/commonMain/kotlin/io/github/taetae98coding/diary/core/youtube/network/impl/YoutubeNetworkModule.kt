package io.github.taetae98coding.diary.core.youtube.network.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.taetae98coding.diary.core.youtube.network.impl.di.YoutubeHttpClient
import io.github.taetae98coding.diary.library.ktor.createJsonHttpClient
import io.github.taetae98coding.diary.library.ktor.createPlatformHttpClientEngine
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine

@BindingContainer
@ContributesTo(AppScope::class)
public object YoutubeNetworkModule {
    @Provides
    @SingleIn(AppScope::class)
    @YoutubeHttpClient
    internal fun providesYoutubeHttpClient(): HttpClient = createYoutubeHttpClient()
}

internal fun createYoutubeHttpClient(engine: HttpClientEngine = createPlatformHttpClientEngine()): HttpClient =
    createJsonHttpClient(
        baseUrl = YOUTUBE_BASE_URL,
        engine = engine,
    )

private const val YOUTUBE_BASE_URL = "https://www.youtube.com/"
