package io.github.taetae98coding.diary.core.ipnetwork.impl

import io.github.taetae98coding.diary.core.ipnetwork.impl.di.IpHttpClient
import io.github.taetae98coding.diary.core.ipnetwork.impl.di.IpHttpClientEngine
import io.github.taetae98coding.diary.library.ktor.createPlatformHttpClientEngine
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
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
public class IpNetworkModule {
    @Single
    @IpHttpClientEngine
    internal fun providesIpHttpClientEngine(): HttpClientEngine = createPlatformHttpClientEngine()

    @Single
    @IpHttpClient
    internal fun providesIpHttpClient(
        @IpHttpClientEngine
        engine: HttpClientEngine,
    ): HttpClient =
        HttpClient(engine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    },
                )
            }

            install(DefaultRequest) {
                url("http://ip-api.com/")
                url.parameters.append("fields", LATITUDE_LONGITUDE_FIELDS)
            }
        }

    public companion object {
        // lat(64) + lon(128)
        private const val LATITUDE_LONGITUDE_FIELDS = "192"
    }
}
