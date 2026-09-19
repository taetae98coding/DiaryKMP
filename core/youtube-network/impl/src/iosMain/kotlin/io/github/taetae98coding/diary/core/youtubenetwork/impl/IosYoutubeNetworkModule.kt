package io.github.taetae98coding.diary.core.youtubenetwork.impl

import io.github.taetae98coding.diary.core.youtubenetwork.impl.di.YoutubeHttpClientEngine
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
public class IosYoutubeNetworkModule {
    @Single
    @YoutubeHttpClientEngine
    internal fun providesYoutubeHttpClientEngine(): HttpClientEngine = Darwin.create()
}
