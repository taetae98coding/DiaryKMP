package io.github.taetae98coding.diary.core.naver.network.impl

import io.github.taetae98coding.diary.core.naver.network.impl.di.NaverHttpClientEngine
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
public class IosNaverNetworkModule {
    @Single
    @NaverHttpClientEngine
    internal fun providesNaverHttpClientEngine(): HttpClientEngine = Darwin.create()
}
