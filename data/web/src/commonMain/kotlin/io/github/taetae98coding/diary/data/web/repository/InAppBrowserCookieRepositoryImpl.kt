package io.github.taetae98coding.diary.data.web.repository

import io.github.taetae98coding.diary.core.browsercookie.api.datasource.InAppBrowserCookieLocalDataSource
import io.github.taetae98coding.diary.core.model.browser.BrowserCookie
import io.github.taetae98coding.diary.data.web.mapper.toLocal
import io.github.taetae98coding.diary.domain.web.repository.InAppBrowserCookieRepository
import org.koin.core.annotation.Factory

@Factory
internal class InAppBrowserCookieRepositoryImpl(
    private val inAppBrowserCookieLocalDataSource: InAppBrowserCookieLocalDataSource,
) : InAppBrowserCookieRepository {
    override suspend fun upsert(cookieList: List<BrowserCookie>) {
        inAppBrowserCookieLocalDataSource.upsert(cookieList = cookieList.map { cookie -> cookie.toLocal() })
    }
}
