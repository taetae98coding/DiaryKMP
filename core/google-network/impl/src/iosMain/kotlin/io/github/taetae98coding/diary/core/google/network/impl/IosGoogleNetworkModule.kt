package io.github.taetae98coding.diary.core.google.network.impl

import io.github.taetae98coding.diary.core.google.network.impl.di.GoogleHttpClientEngine
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
public class IosGoogleNetworkModule {
    @Single
    @GoogleHttpClientEngine
    internal fun providesGoogleHttpClientEngine(): HttpClientEngine = Darwin.create()
}
