package io.github.taetae98coding.diary.core.weather.network.impl

import io.github.taetae98coding.diary.core.weather.network.impl.di.WeatherHttpClient
import io.github.taetae98coding.diary.library.ktor.createJsonHttpClient
import io.github.taetae98coding.diary.library.ktor.createPlatformHttpClientEngine
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
@Configuration
public class WeatherNetworkModule {
    @Single
    @WeatherHttpClient
    internal fun providesWeatherHttpClient(config: OpenWeatherApiConfig): HttpClient = createWeatherHttpClient(config = config)
}

internal fun createWeatherHttpClient(
    config: OpenWeatherApiConfig,
    engine: HttpClientEngine = createPlatformHttpClientEngine(),
): HttpClient =
    createJsonHttpClient(
        baseUrl = OPEN_WEATHER_BASE_URL,
        engine = engine,
    ) {
        url {
            parameters.append(APP_ID_PARAMETER, config.appId)
            parameters.append(UNITS_PARAMETER, METRIC_UNITS)
        }
    }

private const val OPEN_WEATHER_BASE_URL = "https://api.openweathermap.org/"
private const val APP_ID_PARAMETER = "appid"
private const val UNITS_PARAMETER = "units"
private const val METRIC_UNITS = "metric"
