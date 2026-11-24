package io.github.taetae98coding.diary.core.google.network.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.taetae98coding.diary.core.google.network.impl.di.GoogleHttpClient
import io.github.taetae98coding.diary.library.ktor.createJsonHttpClient
import io.github.taetae98coding.diary.library.ktor.createPlatformHttpClientEngine
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

@BindingContainer
@ContributesTo(AppScope::class)
public object GoogleNetworkModule {
    @Provides
    @SingleIn(AppScope::class)
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
