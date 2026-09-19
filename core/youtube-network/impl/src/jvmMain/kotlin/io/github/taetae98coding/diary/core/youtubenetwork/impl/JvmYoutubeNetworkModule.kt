package io.github.taetae98coding.diary.core.youtubenetwork.impl

import io.github.taetae98coding.diary.core.youtubenetwork.impl.di.YoutubeHttpClientEngine
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
public class JvmYoutubeNetworkModule {
    @Single
    @YoutubeHttpClientEngine
    internal fun providesYoutubeHttpClientEngine(): HttpClientEngine = OkHttp.create()
}
