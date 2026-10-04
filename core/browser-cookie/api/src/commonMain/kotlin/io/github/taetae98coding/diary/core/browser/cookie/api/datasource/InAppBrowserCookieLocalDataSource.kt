package io.github.taetae98coding.diary.core.browser.cookie.api.datasource

import io.github.taetae98coding.diary.core.browser.cookie.api.entity.BrowserCookieLocalEntity

public interface InAppBrowserCookieLocalDataSource {
    public suspend fun upsert(cookieList: List<BrowserCookieLocalEntity>)

    public suspend fun deleteAll()
}
