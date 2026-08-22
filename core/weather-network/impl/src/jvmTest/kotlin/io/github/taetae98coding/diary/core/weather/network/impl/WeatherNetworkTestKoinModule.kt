package io.github.taetae98coding.diary.core.weather.network.impl

import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
internal data object WeatherNetworkTestKoinModule {
    // URL 쿼리 값 비교가 인코딩에 흔들리지 않도록 영숫자로 고정한다.
    val config: OpenWeatherApiConfig = OpenWeatherApiConfig(appId = "testAppId")

    @Single
    fun providesOpenWeatherApiConfig(): OpenWeatherApiConfig = config
}
