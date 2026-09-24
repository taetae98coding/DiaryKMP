package io.github.taetae98coding.diary.core.calendar.network.impl

import io.github.taetae98coding.diary.core.calendar.network.impl.di.CalendarHttpClient
import io.github.taetae98coding.diary.core.calendar.network.impl.di.CalendarHttpClientEngine
import io.github.taetae98coding.diary.library.ktor.createPlatformHttpClientEngine
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
@Configuration
@ComponentScan
public class CalendarNetworkModule {
    @Single
    @CalendarHttpClientEngine
    internal fun providesCalendarHttpClientEngine(): HttpClientEngine = createPlatformHttpClientEngine()

    @Single
    @CalendarHttpClient
    internal fun providesCalendarHttpClient(
        @CalendarHttpClientEngine
        engine: HttpClientEngine,
    ): HttpClient =
        HttpClient(engine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    },
                )
            }

            install(DefaultRequest) {
                url(BASE_URL)
            }
        }

    public companion object {
        private const val BASE_URL = "https://taetae98coding.github.io/CalendarApi/"
    }
}
