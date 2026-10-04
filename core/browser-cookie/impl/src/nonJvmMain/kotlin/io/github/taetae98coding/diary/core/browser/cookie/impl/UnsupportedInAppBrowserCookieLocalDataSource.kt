package io.github.taetae98coding.diary.core.browser.cookie.impl

import io.github.taetae98coding.diary.core.browser.cookie.api.datasource.InAppBrowserCookieLocalDataSource
import io.github.taetae98coding.diary.core.browser.cookie.api.entity.BrowserCookieLocalEntity
import org.koin.core.annotation.Factory

@Factory
internal class UnsupportedInAppBrowserCookieLocalDataSource : InAppBrowserCookieLocalDataSource {
    override suspend fun upsert(cookieList: List<BrowserCookieLocalEntity>): Unit = error("In-app browser cookies are not supported on this platform.")

    override suspend fun deleteAll(): Unit = error("In-app browser cookies are not supported on this platform.")
}
