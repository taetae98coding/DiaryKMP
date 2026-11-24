package io.github.taetae98coding.diary.core.calendar.network.impl.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.calendar.network.api.datasource.HolidayRemoteDataSource
import io.github.taetae98coding.diary.core.calendar.network.api.entity.HolidayCountryRemoteEntity
import io.github.taetae98coding.diary.core.calendar.network.api.entity.HolidayRemoteEntity
import io.github.taetae98coding.diary.core.calendar.network.impl.di.CalendarHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode

@Inject
@ContributesBinding(AppScope::class)
internal class HolidayRemoteDataSourceImpl(
    @CalendarHttpClient
    private val httpClient: HttpClient,
) : HolidayRemoteDataSource {
    override suspend fun read(
        country: HolidayCountryRemoteEntity,
        year: Int,
    ): List<HolidayRemoteEntity> {
        val response = httpClient.get("holiday/${country.pathSegment}/$year.json")

        return if (response.status == HttpStatusCode.NotFound) {
            emptyList()
        } else {
            response.body()
        }
    }
}
