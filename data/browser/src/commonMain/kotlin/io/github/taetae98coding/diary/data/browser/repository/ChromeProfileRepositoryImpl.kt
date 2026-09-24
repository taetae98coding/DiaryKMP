package io.github.taetae98coding.diary.data.browser.repository

import io.github.taetae98coding.diary.core.browsercookie.api.datasource.ChromeProfileLocalDataSource
import io.github.taetae98coding.diary.core.model.browser.ChromeProfile
import io.github.taetae98coding.diary.data.browser.mapper.toDomain
import io.github.taetae98coding.diary.domain.browser.repository.ChromeProfileRepository
import org.koin.core.annotation.Factory

@Factory
internal class ChromeProfileRepositoryImpl(
    private val chromeProfileLocalDataSource: ChromeProfileLocalDataSource,
) : ChromeProfileRepository {
    override suspend fun findAll(): List<ChromeProfile> =
        chromeProfileLocalDataSource
            .findAll()
            .map { entity -> entity.toDomain() }
}
