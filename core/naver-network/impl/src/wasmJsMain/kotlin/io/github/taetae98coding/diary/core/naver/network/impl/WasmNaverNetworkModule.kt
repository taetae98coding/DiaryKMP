package io.github.taetae98coding.diary.core.naver.network.impl

import io.github.taetae98coding.diary.core.naver.network.impl.di.NaverHttpClientEngine
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
public class WasmNaverNetworkModule {
    @Single
    @NaverHttpClientEngine
    internal fun providesNaverHttpClientEngine(): HttpClientEngine = Js.create()
}
