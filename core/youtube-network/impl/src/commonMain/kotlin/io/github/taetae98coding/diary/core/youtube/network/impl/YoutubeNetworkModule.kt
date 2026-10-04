package io.github.taetae98coding.diary.core.youtube.network.impl

import io.github.taetae98coding.diary.core.youtube.network.impl.di.YoutubeHttpClient
import io.github.taetae98coding.diary.library.ktor.createJsonHttpClient
import io.github.taetae98coding.diary.library.ktor.createPlatformHttpClientEngine
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
@Configuration
public class YoutubeNetworkModule {
    @Single
    @YoutubeHttpClient
    internal fun providesYoutubeHttpClient(): HttpClient = createYoutubeHttpClient()
}

internal fun createYoutubeHttpClient(engine: HttpClientEngine = createPlatformHttpClientEngine()): HttpClient =
    createJsonHttpClient(
        baseUrl = YOUTUBE_BASE_URL,
        engine = engine,
    )

private const val YOUTUBE_BASE_URL = "https://www.youtube.com/"
