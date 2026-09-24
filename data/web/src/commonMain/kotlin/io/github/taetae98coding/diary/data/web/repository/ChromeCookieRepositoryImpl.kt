package io.github.taetae98coding.diary.data.web.repository

import io.github.taetae98coding.diary.core.browsercookie.api.datasource.ChromeCookieLocalDataSource
import io.github.taetae98coding.diary.core.model.browser.BrowserCookie
import io.github.taetae98coding.diary.data.web.mapper.toDomain
import io.github.taetae98coding.diary.domain.web.repository.ChromeCookieRepository
import org.koin.core.annotation.Factory

@Factory
internal class ChromeCookieRepositoryImpl(
    private val chromeCookieLocalDataSource: ChromeCookieLocalDataSource,
) : ChromeCookieRepository {
    override suspend fun findByDomain(
        profileDirectory: String,
        domainSet: Set<String>,
    ): List<BrowserCookie> =
        chromeCookieLocalDataSource
            .findByDomain(profileDirectory = profileDirectory, domainSet = domainSet)
            .map { entity -> entity.toDomain() }
}
