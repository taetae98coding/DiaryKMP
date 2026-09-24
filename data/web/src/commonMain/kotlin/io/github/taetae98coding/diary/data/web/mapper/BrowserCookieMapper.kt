package io.github.taetae98coding.diary.data.web.mapper

import io.github.taetae98coding.diary.core.browsercookie.api.entity.BrowserCookieLocalEntity
import io.github.taetae98coding.diary.core.browsercookie.api.entity.BrowserCookieSameSiteLocalEntity
import io.github.taetae98coding.diary.core.model.browser.BrowserCookie
import io.github.taetae98coding.diary.core.model.browser.BrowserCookieSameSite

internal fun BrowserCookieLocalEntity.toDomain(): BrowserCookie =
    BrowserCookie(
        name = name,
        value = value,
        domain = domain,
        path = path,
        expiresAt = expiresAt,
        isSecure = isSecure,
        isHttpOnly = isHttpOnly,
        sameSite = sameSite.toDomain(),
    )

internal fun BrowserCookie.toLocal(): BrowserCookieLocalEntity =
    BrowserCookieLocalEntity(
        name = name,
        value = value,
        domain = domain,
        path = path,
        expiresAt = expiresAt,
        isSecure = isSecure,
        isHttpOnly = isHttpOnly,
        sameSite = sameSite.toLocal(),
    )

internal fun BrowserCookieSameSiteLocalEntity.toDomain(): BrowserCookieSameSite =
    when (this) {
        BrowserCookieSameSiteLocalEntity.UNSPECIFIED -> BrowserCookieSameSite.UNSPECIFIED
        BrowserCookieSameSiteLocalEntity.NONE -> BrowserCookieSameSite.NONE
        BrowserCookieSameSiteLocalEntity.LAX -> BrowserCookieSameSite.LAX
        BrowserCookieSameSiteLocalEntity.STRICT -> BrowserCookieSameSite.STRICT
    }

internal fun BrowserCookieSameSite.toLocal(): BrowserCookieSameSiteLocalEntity =
    when (this) {
        BrowserCookieSameSite.UNSPECIFIED -> BrowserCookieSameSiteLocalEntity.UNSPECIFIED
        BrowserCookieSameSite.NONE -> BrowserCookieSameSiteLocalEntity.NONE
        BrowserCookieSameSite.LAX -> BrowserCookieSameSiteLocalEntity.LAX
        BrowserCookieSameSite.STRICT -> BrowserCookieSameSiteLocalEntity.STRICT
    }
