package io.github.taetae98coding.diary.data.browser.repository

import io.github.taetae98coding.diary.core.browsercookie.api.datasource.ChromeCookieLocalDataSource
import io.github.taetae98coding.diary.core.model.browser.BrowserCookie
import io.github.taetae98coding.diary.data.browser.mapper.toDomain
import io.github.taetae98coding.diary.domain.browser.repository.ChromeCookieRepository
import org.koin.core.annotation.Factory

@Factory
internal class ChromeCookieRepositoryImpl(
    private val chromeCookieLocalDataSource: ChromeCookieLocalDataSource,
) : ChromeCookieRepository {
    override suspend fun findAll(profileDirectory: String): List<BrowserCookie> =
        chromeCookieLocalDataSource
            .findAll(profileDirectory = profileDirectory)
            .map { entity -> entity.toDomain() }
}
