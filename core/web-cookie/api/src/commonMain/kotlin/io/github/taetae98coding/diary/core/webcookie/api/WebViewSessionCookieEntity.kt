package io.github.taetae98coding.diary.core.webcookie.api

public data class WebViewSessionCookieEntity(
    val cookieList: List<WebViewCookieEntity>,
    val siteUrlSet: Set<String>,
)
