package io.github.taetae98coding.diary.core.model.web

public data class WebLoginCookie(
    val name: String,
    val value: String,
    val domain: String,
    val path: String,
    val isSecure: Boolean,
    val isHttpOnly: Boolean,
    val sameSite: WebLoginCookieSameSite,
)

public enum class WebLoginCookieSameSite {
    UNSPECIFIED,
    NONE,
    LAX,
    STRICT,
}
