package io.github.taetae98coding.diary.core.naver.network.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.taetae98coding.diary.core.naver.network.impl.di.NaverHttpClient
import io.github.taetae98coding.diary.library.ktor.createJsonHttpClient
import io.github.taetae98coding.diary.library.ktor.createPlatformHttpClientEngine
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.header

@BindingContainer
@ContributesTo(AppScope::class)
public object NaverNetworkModule {
    @Provides
    @SingleIn(AppScope::class)
    @NaverHttpClient
    internal fun providesNaverHttpClient(config: NaverOpenApiConfig): HttpClient = createNaverHttpClient(config = config)
}

internal fun createNaverHttpClient(
    config: NaverOpenApiConfig,
    engine: HttpClientEngine = createPlatformHttpClientEngine(),
): HttpClient =
    createJsonHttpClient(
        baseUrl = NAVER_OPEN_API_BASE_URL,
        engine = engine,
    ) {
        header(CLIENT_ID_HEADER, config.clientId)
        header(CLIENT_SECRET_HEADER, config.clientSecret)
    }

private const val NAVER_OPEN_API_BASE_URL = "https://openapi.naver.com/"
private const val CLIENT_ID_HEADER = "X-Naver-Client-Id"
private const val CLIENT_SECRET_HEADER = "X-Naver-Client-Secret"
