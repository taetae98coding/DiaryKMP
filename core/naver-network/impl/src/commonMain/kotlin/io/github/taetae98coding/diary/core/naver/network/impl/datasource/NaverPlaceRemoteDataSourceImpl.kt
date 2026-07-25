package io.github.taetae98coding.diary.core.naver.network.impl.datasource

import io.github.taetae98coding.diary.core.naver.network.api.datasource.NaverPlaceRemoteDataSource
import io.github.taetae98coding.diary.core.naver.network.api.entity.NaverPlaceRemoteEntity
import io.github.taetae98coding.diary.core.naver.network.impl.di.NaverHttpClient
import io.github.taetae98coding.diary.core.naver.network.impl.entity.NaverPlaceSearchResponseEntity
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.koin.core.annotation.Factory

@Factory
internal class NaverPlaceRemoteDataSourceImpl(
    @NaverHttpClient
    private val httpClient: HttpClient,
) : NaverPlaceRemoteDataSource {
    override suspend fun search(query: String): List<NaverPlaceRemoteEntity> =
        httpClient
            .get("v1/search/local.json") {
                parameter("query", query)
                parameter("display", MAX_DISPLAY)
            }.body<NaverPlaceSearchResponseEntity>()
            .items

    companion object {
        private const val MAX_DISPLAY = 5
    }
}
