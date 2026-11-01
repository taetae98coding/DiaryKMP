package io.github.taetae98coding.diary.core.webcookie.api

// domain이 `.`으로 시작하면 하위 도메인에도 보내는 쿠키이고, 그렇지 않으면 그 호스트에만 보내는 쿠키다.
public data class WebViewCookieEntity(
    val name: String,
    val value: String,
    val domain: String,
    val path: String,
    val isSecure: Boolean,
    val isHttpOnly: Boolean,
    val sameSite: WebViewCookieSameSiteEntity,
)

public enum class WebViewCookieSameSiteEntity {
    UNSPECIFIED,
    NONE,
    LAX,
    STRICT,
}
