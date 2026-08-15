package io.github.taetae98coding.diary.core.webnetwork.impl

import io.github.taetae98coding.diary.core.webnetwork.impl.di.WebPageHttpClientEngine
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
public class WasmWebNetworkModule {
    @Single
    @WebPageHttpClientEngine
    internal fun providesWebPageHttpClientEngine(): HttpClientEngine = Js.create()
}
