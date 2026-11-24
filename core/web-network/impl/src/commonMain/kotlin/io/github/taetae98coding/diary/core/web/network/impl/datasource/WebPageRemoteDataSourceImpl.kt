package io.github.taetae98coding.diary.core.web.network.impl.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.web.network.api.datasource.WebPageRemoteDataSource
import io.github.taetae98coding.diary.core.web.network.api.entity.WebPageHeaderRemoteEntity
import io.github.taetae98coding.diary.core.web.network.api.entity.WebPageRemoteEntity
import io.github.taetae98coding.diary.core.web.network.impl.di.WebPageHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request

@Inject
@ContributesBinding(AppScope::class)
internal class WebPageRemoteDataSourceImpl(
    @WebPageHttpClient
    private val httpClient: HttpClient,
) : WebPageRemoteDataSource {
    override suspend fun read(
        url: String,
        headerList: List<WebPageHeaderRemoteEntity>,
    ): WebPageRemoteEntity {
        val response =
            httpClient.get(url) {
                headerList.forEach { requestHeader -> header(requestHeader.name, requestHeader.value) }
            }

        return WebPageRemoteEntity(
            url = response.request.url.toString(),
            body = response.bodyAsText(),
        )
    }
}
