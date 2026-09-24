package io.github.taetae98coding.diary.core.browsercookie.api.datasource

import io.github.taetae98coding.diary.core.browsercookie.api.entity.BrowserCookieLocalEntity

public interface InAppBrowserCookieLocalDataSource {
    public suspend fun upsert(cookieList: List<BrowserCookieLocalEntity>)

    public suspend fun deleteAll()
}
