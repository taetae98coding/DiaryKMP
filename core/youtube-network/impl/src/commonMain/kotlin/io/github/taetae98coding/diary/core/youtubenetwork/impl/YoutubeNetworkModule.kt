package io.github.taetae98coding.diary.core.youtubenetwork.impl

import io.github.taetae98coding.diary.core.youtubenetwork.impl.di.YoutubeHttpClient
import io.github.taetae98coding.diary.core.youtubenetwork.impl.di.YoutubeHttpClientEngine
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
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
    internal fun providesYoutubeHttpClient(
        @YoutubeHttpClientEngine
        engine: HttpClientEngine,
    ): HttpClient =
        HttpClient(engine) {
            expectSuccess = true

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    },
                )
            }

            install(DefaultRequest) {
                url("https://www.youtube.com/")
            }
        }
}
