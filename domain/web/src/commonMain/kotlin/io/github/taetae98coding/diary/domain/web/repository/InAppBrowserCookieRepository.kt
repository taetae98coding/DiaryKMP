package io.github.taetae98coding.diary.domain.web.repository

import io.github.taetae98coding.diary.core.model.browser.BrowserCookie

public interface InAppBrowserCookieRepository {
    public suspend fun upsert(cookieList: List<BrowserCookie>)
}
