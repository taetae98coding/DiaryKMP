package io.github.taetae98coding.diary.core.weather.network.impl

import io.github.taetae98coding.diary.core.weather.network.impl.di.WeatherHttpClient
import io.github.taetae98coding.diary.core.weather.network.impl.di.WeatherHttpClientEngine
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
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
    internal fun providesWeatherHttpClient(
        @WeatherHttpClientEngine
        engine: HttpClientEngine,
        config: OpenWeatherApiConfig,
    ): HttpClient =
        HttpClient(engine) {
            expectSuccess = true

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    },
                )
            }

            install(DefaultRequest) {
                url(BASE_URL)
                url {
                    parameters.append(APP_ID_PARAMETER, config.appId)
                    parameters.append(UNITS_PARAMETER, METRIC_UNITS)
                }
            }
        }

    public companion object {
        private const val BASE_URL = "https://api.openweathermap.org/"
        private const val APP_ID_PARAMETER = "appid"
        private const val UNITS_PARAMETER = "units"
        private const val METRIC_UNITS = "metric"
    }
}
