package io.github.taetae98coding.diary.core.google.network.impl

import io.github.taetae98coding.diary.core.google.network.impl.di.GoogleHttpClient
import io.github.taetae98coding.diary.core.google.network.impl.di.GoogleHttpClientEngine
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
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
public class GoogleNetworkModule {
    @Single
    @GoogleHttpClient
    internal fun providesGoogleHttpClient(
        @GoogleHttpClientEngine
        engine: HttpClientEngine,
        config: GooglePlacesConfig,
    ): HttpClient =
        HttpClient(engine) {
            expectSuccess = true

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    },
                )
            }

            install(DefaultRequest) {
                url(BASE_URL)
                contentType(ContentType.Application.Json)
                header(API_KEY_HEADER, config.apiKey)
            }
        }

    public companion object {
        private const val BASE_URL = "https://places.googleapis.com/"
        private const val API_KEY_HEADER = "X-Goog-Api-Key"
    }
}
