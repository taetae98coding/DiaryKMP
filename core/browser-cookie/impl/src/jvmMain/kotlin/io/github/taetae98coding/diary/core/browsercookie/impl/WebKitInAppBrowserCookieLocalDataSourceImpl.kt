package io.github.taetae98coding.diary.core.browsercookie.impl

import io.github.taetae98coding.diary.core.browsercookie.api.datasource.InAppBrowserCookieLocalDataSource
import io.github.taetae98coding.diary.core.browsercookie.api.entity.BrowserCookieLocalEntity
import io.github.taetae98coding.diary.core.browsercookie.api.entity.BrowserCookieSameSiteLocalEntity
import io.github.taetae98coding.diary.library.webkit.WebKitCookie
import io.github.taetae98coding.diary.library.webkit.WebKitCookieSameSite
import io.github.taetae98coding.diary.library.webkit.WebKitCookieStore
import org.koin.core.annotation.Factory

@Factory
internal class WebKitInAppBrowserCookieLocalDataSourceImpl : InAppBrowserCookieLocalDataSource {
    override suspend fun upsert(cookieList: List<BrowserCookieLocalEntity>) {
        WebKitCookieStore.setCookies(cookieList = cookieList.map { cookie -> cookie.toWebKit() })
    }

    private fun BrowserCookieLocalEntity.toWebKit(): WebKitCookie =
        WebKitCookie(
            name = name,
            value = value,
            domain = domain,
            path = path,
            expiresAtEpochMilliseconds = expiresAt?.toEpochMilliseconds(),
            isSecure = isSecure,
            isHttpOnly = isHttpOnly,
            sameSite = sameSite.toWebKit(),
        )

    private fun BrowserCookieSameSiteLocalEntity.toWebKit(): WebKitCookieSameSite =
        when (this) {
            BrowserCookieSameSiteLocalEntity.UNSPECIFIED -> WebKitCookieSameSite.UNSPECIFIED
            BrowserCookieSameSiteLocalEntity.NONE -> WebKitCookieSameSite.NONE
            BrowserCookieSameSiteLocalEntity.LAX -> WebKitCookieSameSite.LAX
            BrowserCookieSameSiteLocalEntity.STRICT -> WebKitCookieSameSite.STRICT
        }
}
