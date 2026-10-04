package io.github.taetae98coding.diary.core.calendar.network.impl

import io.github.taetae98coding.diary.core.calendar.network.impl.di.CalendarHttpClient
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
public class CalendarNetworkModule {
    @Single
    @CalendarHttpClient
    internal fun providesCalendarHttpClient(): HttpClient = createCalendarHttpClient()
}

internal fun createCalendarHttpClient(engine: HttpClientEngine = createPlatformHttpClientEngine()): HttpClient =
    createJsonHttpClient(
        baseUrl = CALENDAR_API_BASE_URL,
        engine = engine,
        // 자료가 없는 연도는 404로 오므로 데이터 소스가 응답 코드를 직접 판정한다.
        expectSuccess = false,
    )

private const val CALENDAR_API_BASE_URL = "https://taetae98coding.github.io/CalendarApi/"
