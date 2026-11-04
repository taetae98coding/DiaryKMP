package io.github.taetae98coding.diary.library.ktor

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

public val DefaultHttpClientJson: Json =
    Json {
        ignoreUnknownKeys = true
    }

public fun createJsonHttpClient(
    baseUrl: String,
    engine: HttpClientEngine = createPlatformHttpClientEngine(),
    json: Json = DefaultHttpClientJson,
    expectSuccess: Boolean = true,
    configure: HttpClientConfig<*>.() -> Unit = {},
    defaultRequest: DefaultRequest.DefaultRequestBuilder.() -> Unit = {},
): HttpClient =
    HttpClient(engine) {
        this.expectSuccess = expectSuccess

        install(ContentNegotiation) {
            json(json)
        }

        install(RemoteResponseFailureReport)

        install(DefaultRequest) {
            url(baseUrl)
            defaultRequest()
        }

        configure()
    }
