package io.github.taetae98coding.diary.core.google.network.impl

import io.github.taetae98coding.diary.core.google.network.impl.di.GoogleHttpClient
import io.github.taetae98coding.diary.library.ktor.createJsonHttpClient
import io.github.taetae98coding.diary.library.ktor.createPlatformHttpClientEngine
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
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
    internal fun providesGoogleHttpClient(config: GooglePlacesConfig): HttpClient = createGoogleHttpClient(config = config)
}

internal fun createGoogleHttpClient(
    config: GooglePlacesConfig,
    engine: HttpClientEngine = createPlatformHttpClientEngine(),
): HttpClient =
    createJsonHttpClient(
        baseUrl = GOOGLE_PLACES_BASE_URL,
        engine = engine,
        json =
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            },
    ) {
        contentType(ContentType.Application.Json)
        header(API_KEY_HEADER, config.apiKey)
    }

private const val GOOGLE_PLACES_BASE_URL = "https://places.googleapis.com/"
private const val API_KEY_HEADER = "X-Goog-Api-Key"
