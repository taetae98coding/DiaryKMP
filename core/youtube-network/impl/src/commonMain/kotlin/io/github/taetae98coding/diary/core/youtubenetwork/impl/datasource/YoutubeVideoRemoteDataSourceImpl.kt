package io.github.taetae98coding.diary.core.youtubenetwork.impl.datasource

import io.github.taetae98coding.diary.core.youtubenetwork.api.datasource.YoutubeVideoRemoteDataSource
import io.github.taetae98coding.diary.core.youtubenetwork.api.entity.YoutubeVideoRemoteEntity
import io.github.taetae98coding.diary.core.youtubenetwork.impl.di.YoutubeHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.koin.core.annotation.Factory

@Factory
internal class YoutubeVideoRemoteDataSourceImpl(
    @YoutubeHttpClient
    private val httpClient: HttpClient,
) : YoutubeVideoRemoteDataSource {
    override suspend fun fetch(link: String): YoutubeVideoRemoteEntity =
        httpClient
            .get("oembed") {
                parameter("url", link)
                parameter("format", "json")
            }.body()
}
