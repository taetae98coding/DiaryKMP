package io.github.taetae98coding.diary.core.youtube.network.impl.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.youtube.network.api.datasource.YoutubeVideoRemoteDataSource
import io.github.taetae98coding.diary.core.youtube.network.api.entity.YoutubeVideoRemoteEntity
import io.github.taetae98coding.diary.core.youtube.network.impl.di.YoutubeHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

@Inject
@ContributesBinding(AppScope::class)
internal class YoutubeVideoRemoteDataSourceImpl(
    @YoutubeHttpClient
    private val httpClient: HttpClient,
) : YoutubeVideoRemoteDataSource {
    override suspend fun read(link: String): YoutubeVideoRemoteEntity =
        httpClient
            .get("oembed") {
                parameter("url", link)
                parameter("format", "json")
            }.body()
}
