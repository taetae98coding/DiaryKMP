package io.github.taetae98coding.diary.core.gemini.network.impl

import io.github.taetae98coding.diary.core.gemini.network.impl.di.GeminiHttpClient
import io.github.taetae98coding.diary.core.gemini.network.impl.di.GeminiJson
import io.github.taetae98coding.diary.library.ktor.createJsonHttpClient
import io.github.taetae98coding.diary.library.ktor.createPlatformHttpClientEngine
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
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
public class GeminiNetworkModule {
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
    ) {
        contentType(ContentType.Application.Json)
    }

private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/"
