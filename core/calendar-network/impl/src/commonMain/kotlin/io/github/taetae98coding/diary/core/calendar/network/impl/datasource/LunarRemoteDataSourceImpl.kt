package io.github.taetae98coding.diary.core.calendar.network.impl.datasource

import io.github.taetae98coding.diary.core.calendar.network.api.datasource.LunarRemoteDataSource
import io.github.taetae98coding.diary.core.calendar.network.api.entity.LunarDateRemoteEntity
import io.github.taetae98coding.diary.core.calendar.network.impl.di.CalendarHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import org.koin.core.annotation.Factory

// CalendarApi는 국가별 경로로 음력 자료를 내려주지만 어느 국가든 같은 한국 음력 자료다.
private const val LUNAR_COUNTRY_PATH_SEGMENT = "kr"

@Factory
internal class LunarRemoteDataSourceImpl(
    @CalendarHttpClient
    private val client: HttpClient,
) : LunarRemoteDataSource {
    override suspend fun get(year: Int): List<LunarDateRemoteEntity> {
        val response = client.get("lunar/$LUNAR_COUNTRY_PATH_SEGMENT/$year.json")

        return if (response.status == HttpStatusCode.NotFound) {
            emptyList()
        } else {
            response.body()
        }
    }
}
