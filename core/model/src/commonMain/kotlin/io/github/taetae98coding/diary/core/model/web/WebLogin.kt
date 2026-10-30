package io.github.taetae98coding.diary.core.model.web

public data class WebLogin(
    val sessionCookieList: List<WebLoginCookie> = emptyList(),
    val siteUrlSet: Set<String> = emptySet(),
)
