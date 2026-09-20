package io.github.taetae98coding.diary.data.web.repository

import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.core.model.web.WebPage
import io.github.taetae98coding.diary.core.webnetwork.api.datasource.WebPageRemoteDataSource
import io.github.taetae98coding.diary.data.web.mapper.toDomain
import io.github.taetae98coding.diary.data.web.mapper.toRemote
import io.github.taetae98coding.diary.domain.web.repository.WebPageRepository
import org.koin.core.annotation.Factory

@Factory
internal class WebPageRepositoryImpl(
    private val webPageRemoteDataSource: WebPageRemoteDataSource,
) : WebPageRepository {
    override suspend fun fetch(
        url: String,
        headerList: List<WebHeader>,
    ): WebPage =
        webPageRemoteDataSource
            .get(
                url = url,
                headerList = headerList.map { header -> header.toRemote() },
            ).toDomain()
}
