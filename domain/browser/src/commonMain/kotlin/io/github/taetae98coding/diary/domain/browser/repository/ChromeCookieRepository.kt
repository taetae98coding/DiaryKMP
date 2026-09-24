package io.github.taetae98coding.diary.domain.browser.repository

import io.github.taetae98coding.diary.core.model.browser.BrowserCookie

public interface ChromeCookieRepository {
    public suspend fun findAll(profileDirectory: String): List<BrowserCookie>
}
