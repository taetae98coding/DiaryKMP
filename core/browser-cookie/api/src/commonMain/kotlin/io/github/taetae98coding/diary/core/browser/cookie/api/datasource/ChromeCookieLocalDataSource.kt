package io.github.taetae98coding.diary.core.browser.cookie.api.datasource

import io.github.taetae98coding.diary.core.browser.cookie.api.entity.BrowserCookieLocalEntity

public interface ChromeCookieLocalDataSource {
    public val isSupported: Boolean

    public suspend fun readCookieList(profileDirectory: String): List<BrowserCookieLocalEntity>
}
