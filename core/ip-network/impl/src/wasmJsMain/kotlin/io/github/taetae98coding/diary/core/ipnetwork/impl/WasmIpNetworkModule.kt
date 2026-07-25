package io.github.taetae98coding.diary.core.ipnetwork.impl

import io.github.taetae98coding.diary.core.ipnetwork.impl.di.IpHttpClientEngine
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
public class WasmIpNetworkModule {
    @Single
    @IpHttpClientEngine
    internal fun providesIpHttpClientEngine(): HttpClientEngine = Js.create()
}
