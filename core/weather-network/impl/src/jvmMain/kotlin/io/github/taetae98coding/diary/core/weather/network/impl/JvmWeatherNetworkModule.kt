package io.github.taetae98coding.diary.core.weather.network.impl

import io.github.taetae98coding.diary.core.weather.network.impl.di.WeatherHttpClientEngine
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
public class JvmWeatherNetworkModule {
    @Single
    @WeatherHttpClientEngine
    internal fun providesWeatherHttpClientEngine(): HttpClientEngine = OkHttp.create()
}
