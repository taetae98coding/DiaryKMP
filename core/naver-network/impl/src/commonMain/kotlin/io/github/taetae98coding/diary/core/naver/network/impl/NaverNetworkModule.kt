package io.github.taetae98coding.diary.core.naver.network.impl

import io.github.taetae98coding.diary.core.naver.network.impl.di.NaverHttpClient
import io.github.taetae98coding.diary.core.naver.network.impl.di.NaverHttpClientEngine
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
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
public class NaverNetworkModule {
    @Single
    @NaverHttpClient
    internal fun providesNaverHttpClient(
        @NaverHttpClientEngine
        engine: HttpClientEngine,
        config: NaverOpenApiConfig,
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
                url(BASE_URL)
                header(CLIENT_ID_HEADER, config.clientId)
                header(CLIENT_SECRET_HEADER, config.clientSecret)
            }
        }

    public companion object {
        private const val BASE_URL = "https://openapi.naver.com/"
        private const val CLIENT_ID_HEADER = "X-Naver-Client-Id"
        private const val CLIENT_SECRET_HEADER = "X-Naver-Client-Secret"
    }
}
