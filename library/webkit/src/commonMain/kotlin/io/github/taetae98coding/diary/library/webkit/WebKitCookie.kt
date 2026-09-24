package io.github.taetae98coding.diary.library.webkit

public data class WebKitCookie(
    val name: String,
    val value: String,
    val domain: String,
    val path: String,
    val expiresAtEpochMilliseconds: Long?,
    val isSecure: Boolean,
    val isHttpOnly: Boolean,
    val sameSite: WebKitCookieSameSite,
)

public enum class WebKitCookieSameSite {
    UNSPECIFIED,
    NONE,
    LAX,
    STRICT,
}
