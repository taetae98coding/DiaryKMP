package io.github.taetae98coding.diary.core.webnetwork.impl.datasource

import io.github.taetae98coding.diary.core.webnetwork.api.datasource.WebPageRemoteDataSource
import io.github.taetae98coding.diary.core.webnetwork.api.entity.WebPageHeaderRemoteEntity
import io.github.taetae98coding.diary.core.webnetwork.api.entity.WebPageRemoteEntity
import io.github.taetae98coding.diary.core.webnetwork.impl.di.WebPageHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import org.koin.core.annotation.Factory

@Factory
internal class WebPageRemoteDataSourceImpl(
    @WebPageHttpClient
    private val httpClient: HttpClient,
) : WebPageRemoteDataSource {
    override suspend fun get(
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
