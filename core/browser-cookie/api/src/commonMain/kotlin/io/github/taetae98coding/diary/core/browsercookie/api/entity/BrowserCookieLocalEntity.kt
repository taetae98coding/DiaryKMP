package io.github.taetae98coding.diary.core.browsercookie.api.entity

import kotlin.time.Instant

public data class BrowserCookieLocalEntity(
    val name: String,
    val value: String,
    val domain: String,
    val path: String,
    val expiresAt: Instant?,
    val isSecure: Boolean,
    val isHttpOnly: Boolean,
    val sameSite: BrowserCookieSameSiteLocalEntity,
)
