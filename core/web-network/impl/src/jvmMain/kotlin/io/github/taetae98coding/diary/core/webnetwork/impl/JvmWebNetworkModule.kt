package io.github.taetae98coding.diary.core.webnetwork.impl

import io.github.taetae98coding.diary.core.webnetwork.impl.di.WebPageHttpClientEngine
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
public class JvmWebNetworkModule {
    @Single
    @WebPageHttpClientEngine
    internal fun providesWebPageHttpClientEngine(): HttpClientEngine = OkHttp.create()
}
