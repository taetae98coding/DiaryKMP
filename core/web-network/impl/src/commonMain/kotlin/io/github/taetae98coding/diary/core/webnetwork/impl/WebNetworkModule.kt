package io.github.taetae98coding.diary.core.webnetwork.impl

import io.github.taetae98coding.diary.core.webnetwork.impl.di.WebPageHttpClient
import io.github.taetae98coding.diary.core.webnetwork.impl.di.WebPageHttpClientEngine
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
    @WebPageHttpClientEngine
    internal fun providesWebPageHttpClientEngine(): HttpClientEngine = createPlatformHttpClientEngine()

    @Single
    @WebPageHttpClient
    internal fun providesWebPageHttpClient(
        @WebPageHttpClientEngine
        engine: HttpClientEngine,
    ): HttpClient = HttpClient(engine)
}
