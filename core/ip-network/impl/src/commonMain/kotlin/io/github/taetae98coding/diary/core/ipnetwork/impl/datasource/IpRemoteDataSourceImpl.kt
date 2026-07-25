package io.github.taetae98coding.diary.core.ipnetwork.impl.datasource

import io.github.taetae98coding.diary.core.ipnetwork.api.datasource.IpRemoteDataSource
import io.github.taetae98coding.diary.core.ipnetwork.api.entity.IpRemoteEntity
import io.github.taetae98coding.diary.core.ipnetwork.impl.di.IpHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.koin.core.annotation.Factory

@Factory
internal class IpRemoteDataSourceImpl(
    @IpHttpClient
    private val httpClient: HttpClient,
) : IpRemoteDataSource {
    override suspend fun get(): IpRemoteEntity = httpClient.get("json").body()
}
