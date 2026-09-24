package io.github.taetae98coding.diary.core.browsercookie.api.datasource

import io.github.taetae98coding.diary.core.browsercookie.api.entity.BrowserCookieLocalEntity

public interface ChromeCookieLocalDataSource {
    public val isSupported: Boolean

    public suspend fun findByDomain(
        profileDirectory: String,
        domainSet: Set<String>,
    ): List<BrowserCookieLocalEntity>
}
