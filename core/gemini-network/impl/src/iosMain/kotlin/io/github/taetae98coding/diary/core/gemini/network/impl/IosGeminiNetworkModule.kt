package io.github.taetae98coding.diary.core.gemini.network.impl

import io.github.taetae98coding.diary.core.gemini.network.impl.di.GeminiHttpClientEngine
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
public class IosGeminiNetworkModule {
    @Single
    @GeminiHttpClientEngine
    internal fun providesGeminiHttpClientEngine(): HttpClientEngine = Darwin.create()
}
