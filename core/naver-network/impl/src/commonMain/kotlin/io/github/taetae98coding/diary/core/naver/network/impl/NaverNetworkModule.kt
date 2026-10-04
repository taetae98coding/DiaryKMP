package io.github.taetae98coding.diary.core.naver.network.impl

import io.github.taetae98coding.diary.core.naver.network.impl.di.NaverHttpClient
import io.github.taetae98coding.diary.library.ktor.createJsonHttpClient
import io.github.taetae98coding.diary.library.ktor.createPlatformHttpClientEngine
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.header
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
@Configuration
public class NaverNetworkModule {
    @Single
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
