package io.github.taetae98coding.diary.core.browsercookie.impl

import io.github.taetae98coding.diary.core.browsercookie.api.datasource.ChromeCookieLocalDataSource
import io.github.taetae98coding.diary.core.browsercookie.api.entity.BrowserCookieLocalEntity

internal object UnsupportedChromeCookieLocalDataSource : ChromeCookieLocalDataSource {
    override val isSupported: Boolean = false

    override suspend fun findAll(profileDirectory: String): List<BrowserCookieLocalEntity> = error("Chrome cookies are not supported on this platform.")
}
