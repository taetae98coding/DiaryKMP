package io.github.taetae98coding.diary.core.gemini.network.impl

import io.github.taetae98coding.diary.core.gemini.network.impl.di.GeminiHttpClient
import io.github.taetae98coding.diary.core.gemini.network.impl.di.GeminiHttpClientEngine
import io.github.taetae98coding.diary.core.gemini.network.impl.di.GeminiJson
import io.github.taetae98coding.diary.library.ktor.createPlatformHttpClientEngine
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
@Configuration
public class GeminiNetworkModule {
    @Single
    @GeminiHttpClientEngine
    internal fun providesGeminiHttpClientEngine(): HttpClientEngine = createPlatformHttpClientEngine()

    @Single
    @GeminiJson
    internal fun providesGeminiJson(): Json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

    @Single
    @GeminiHttpClient
    internal fun providesGeminiHttpClient(
        @GeminiHttpClientEngine
        engine: HttpClientEngine,
        @GeminiJson
        json: Json,
    ): HttpClient =
        HttpClient(engine) {
            expectSuccess = true

            install(ContentNegotiation) {
                json(json)
            }

            install(DefaultRequest) {
                url(BASE_URL)
                contentType(ContentType.Application.Json)
            }
        }

    public companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/"
    }
}
