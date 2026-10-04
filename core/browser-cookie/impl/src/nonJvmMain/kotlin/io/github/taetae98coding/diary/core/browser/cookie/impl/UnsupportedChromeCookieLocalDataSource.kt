package io.github.taetae98coding.diary.core.browser.cookie.impl

import io.github.taetae98coding.diary.core.browser.cookie.api.datasource.ChromeCookieLocalDataSource
import io.github.taetae98coding.diary.core.browser.cookie.api.entity.BrowserCookieLocalEntity
import org.koin.core.annotation.Factory

@Factory
internal class UnsupportedChromeCookieLocalDataSource : ChromeCookieLocalDataSource {
    override val isSupported: Boolean = false

    override suspend fun readCookieList(profileDirectory: String): List<BrowserCookieLocalEntity> = error("Chrome cookies are not supported on this platform.")
}
