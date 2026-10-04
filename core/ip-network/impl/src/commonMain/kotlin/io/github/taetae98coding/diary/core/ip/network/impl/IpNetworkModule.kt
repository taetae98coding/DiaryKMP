package io.github.taetae98coding.diary.core.ip.network.impl

import io.github.taetae98coding.diary.core.ip.network.impl.di.IpHttpClient
import io.github.taetae98coding.diary.library.ktor.createJsonHttpClient
import io.github.taetae98coding.diary.library.ktor.createPlatformHttpClientEngine
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
@Configuration
public class IpNetworkModule {
    @Single
    @IpHttpClient
    internal fun providesIpHttpClient(): HttpClient = createIpHttpClient()
}

internal fun createIpHttpClient(engine: HttpClientEngine = createPlatformHttpClientEngine()): HttpClient =
    createJsonHttpClient(
        baseUrl = IP_API_BASE_URL,
        engine = engine,
    ) {
        url.parameters.append(FIELDS_PARAMETER, (LATITUDE_FIELD or LONGITUDE_FIELD).toString())
    }

// ip-api.com의 무료 이용은 HTTPS를 제공하지 않아 HTTP로 호출한다.
private const val IP_API_BASE_URL = "http://ip-api.com/"
private const val FIELDS_PARAMETER = "fields"
private const val LATITUDE_FIELD = 64
private const val LONGITUDE_FIELD = 128
