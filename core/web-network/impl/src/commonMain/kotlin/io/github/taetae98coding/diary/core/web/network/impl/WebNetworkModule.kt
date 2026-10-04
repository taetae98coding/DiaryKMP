package io.github.taetae98coding.diary.core.web.network.impl

import io.github.taetae98coding.diary.core.web.network.impl.di.WebPageHttpClient
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
public class WebNetworkModule {
    @Single
    @WebPageHttpClient
    internal fun providesWebPageHttpClient(): HttpClient = createWebPageHttpClient()
}

// 임의의 웹 페이지를 받아 본문을 그대로 전달하므로 JSON 설정과 기본 주소를 두지 않는다.
internal fun createWebPageHttpClient(engine: HttpClientEngine = createPlatformHttpClientEngine()): HttpClient =
    HttpClient(engine) {
        expectSuccess = false
    }
