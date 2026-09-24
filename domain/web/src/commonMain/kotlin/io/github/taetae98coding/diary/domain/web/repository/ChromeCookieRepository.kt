package io.github.taetae98coding.diary.domain.web.repository

import io.github.taetae98coding.diary.core.model.browser.BrowserCookie

public interface ChromeCookieRepository {
    public suspend fun findByDomain(
        profileDirectory: String,
        domainSet: Set<String>,
    ): List<BrowserCookie>
}
