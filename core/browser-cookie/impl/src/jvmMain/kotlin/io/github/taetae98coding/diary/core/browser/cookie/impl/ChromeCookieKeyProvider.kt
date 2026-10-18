package io.github.taetae98coding.diary.core.browser.cookie.impl

internal fun interface ChromeCookieKeyProvider {
    suspend fun getKey(): ByteArray
}
