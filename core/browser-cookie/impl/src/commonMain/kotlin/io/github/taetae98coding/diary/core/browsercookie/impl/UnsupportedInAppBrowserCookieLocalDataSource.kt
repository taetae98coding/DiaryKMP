package io.github.taetae98coding.diary.core.browsercookie.impl

import io.github.taetae98coding.diary.core.browsercookie.api.datasource.InAppBrowserCookieLocalDataSource
import io.github.taetae98coding.diary.core.browsercookie.api.entity.BrowserCookieLocalEntity

internal object UnsupportedInAppBrowserCookieLocalDataSource : InAppBrowserCookieLocalDataSource {
    override suspend fun upsert(cookieList: List<BrowserCookieLocalEntity>): Unit = error("In-app browser cookies are not supported on this platform.")
}
