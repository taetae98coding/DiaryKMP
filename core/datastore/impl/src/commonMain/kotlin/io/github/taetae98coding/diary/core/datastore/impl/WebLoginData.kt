package io.github.taetae98coding.diary.core.datastore.impl

import kotlinx.serialization.Serializable

@Serializable
internal data class WebLoginData(
    val sessionCookieList: List<WebLoginCookieData> = emptyList(),
    val siteUrlSet: Set<String> = emptySet(),
)

@Serializable
internal data class WebLoginCookieData(
    val name: String,
    val value: String,
    val domain: String,
    val path: String,
    val isSecure: Boolean,
    val isHttpOnly: Boolean,
    val sameSite: String,
)
