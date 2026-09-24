package io.github.taetae98coding.diary.core.model.browser

import kotlin.time.Instant

public data class BrowserCookie(
    val name: String,
    val value: String,
    val domain: String,
    val path: String,
    val expiresAt: Instant?,
    val isSecure: Boolean,
    val isHttpOnly: Boolean,
    val sameSite: BrowserCookieSameSite,
)
