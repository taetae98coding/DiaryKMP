package io.github.taetae98coding.diary.core.gemini.network.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.taetae98coding.diary.core.gemini.network.impl.di.GeminiHttpClient
import io.github.taetae98coding.diary.core.gemini.network.impl.di.GeminiJson
import io.github.taetae98coding.diary.library.ktor.createJsonHttpClient
import io.github.taetae98coding.diary.library.ktor.createPlatformHttpClientEngine
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

@BindingContainer
@ContributesTo(AppScope::class)
public object GeminiNetworkModule {
    @Provides
    @SingleIn(AppScope::class)
    @GeminiJson
    internal fun providesGeminiJson(): Json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

    @Provides
    @SingleIn(AppScope::class)
    @GeminiHttpClient
    internal fun providesGeminiHttpClient(
        @GeminiJson
        json: Json,
    ): HttpClient = createGeminiHttpClient(json = json)
}

internal fun createGeminiHttpClient(
    json: Json,
    engine: HttpClientEngine = createPlatformHttpClientEngine(),
): HttpClient =
    createJsonHttpClient(
        baseUrl = GEMINI_BASE_URL,
        engine = engine,
        json = json,
        configure = { install(HttpTimeout) },
    ) {
        contentType(ContentType.Application.Json)
    }

private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/"
