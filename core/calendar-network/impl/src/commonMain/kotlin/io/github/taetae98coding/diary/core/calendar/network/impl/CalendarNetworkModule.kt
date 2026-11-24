package io.github.taetae98coding.diary.core.calendar.network.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.taetae98coding.diary.core.calendar.network.impl.di.CalendarHttpClient
import io.github.taetae98coding.diary.library.ktor.createJsonHttpClient
import io.github.taetae98coding.diary.library.ktor.createPlatformHttpClientEngine
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine

@BindingContainer
@ContributesTo(AppScope::class)
public object CalendarNetworkModule {
    @Provides
    @SingleIn(AppScope::class)
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
