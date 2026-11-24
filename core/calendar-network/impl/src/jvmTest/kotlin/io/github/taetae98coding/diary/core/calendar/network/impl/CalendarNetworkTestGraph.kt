package io.github.taetae98coding.diary.core.calendar.network.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.core.calendar.network.api.datasource.HolidayRemoteDataSource
import io.github.taetae98coding.diary.core.calendar.network.api.datasource.LunarRemoteDataSource
import io.github.taetae98coding.diary.core.calendar.network.impl.di.CalendarHttpClient
import io.ktor.client.HttpClient

@DependencyGraph(scope = AppScope::class, excludes = [CalendarNetworkModule::class])
internal interface CalendarNetworkTestGraph {
    val holidayRemoteDataSource: HolidayRemoteDataSource
    val lunarRemoteDataSource: LunarRemoteDataSource

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides @CalendarHttpClient httpClient: HttpClient,
        ): CalendarNetworkTestGraph
    }
}
