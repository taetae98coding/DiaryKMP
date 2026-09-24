package io.github.taetae98coding.diary.core.browsercookie.impl

internal fun interface ChromeCookieKeyProvider {
    suspend fun getKey(): ByteArray
}
