package io.github.taetae98coding.diary.core.datastore.api.web.entity

public data class WebLoginLocalEntity(
    val sessionCookieList: List<WebLoginCookieLocalEntity> = emptyList(),
    val siteUrlSet: Set<String> = emptySet(),
)

public data class WebLoginCookieLocalEntity(
    val name: String,
    val value: String,
    val domain: String,
    val path: String,
    val isSecure: Boolean,
    val isHttpOnly: Boolean,
    val sameSite: WebLoginCookieSameSiteLocalEntity,
)

public enum class WebLoginCookieSameSiteLocalEntity(
    public val persistentValue: String,
) {
    UNSPECIFIED("unspecified"),
    NONE("none"),
    LAX("lax"),
    STRICT("strict"),
}
