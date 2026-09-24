package io.github.taetae98coding.diary.core.calendar.network.impl.datasource

import io.github.taetae98coding.diary.core.calendar.network.api.datasource.HolidayRemoteDataSource
import io.github.taetae98coding.diary.core.calendar.network.api.entity.HolidayCountryRemoteEntity
import io.github.taetae98coding.diary.core.calendar.network.api.entity.HolidayRemoteEntity
import io.github.taetae98coding.diary.core.calendar.network.impl.di.CalendarHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import org.koin.core.annotation.Factory

@Factory
internal class HolidayRemoteDataSourceImpl(
    @CalendarHttpClient
    private val client: HttpClient,
) : HolidayRemoteDataSource {
    override suspend fun get(
        country: HolidayCountryRemoteEntity,
        year: Int,
    ): List<HolidayRemoteEntity> {
        val response = client.get("holiday/${country.pathSegment}/$year.json")

        return if (response.status == HttpStatusCode.NotFound) {
            emptyList()
        } else {
            response.body()
        }
    }
}
