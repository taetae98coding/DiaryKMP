package io.github.taetae98coding.diary.core.webcookie.impl

import android.net.Uri
import io.github.taetae98coding.diary.core.webcookie.api.WebViewCookieEntity
import io.github.taetae98coding.diary.core.webcookie.api.WebViewCookieSameSiteEntity

private const val ATTRIBUTE_SEPARATOR = ";"
private const val VALUE_SEPARATOR = "="
private const val DOMAIN_PREFIX = "."
private const val DEFAULT_PATH = "/"
private const val SECURE_SCHEME = "https://"

private const val DOMAIN_ATTRIBUTE = "domain"
private const val PATH_ATTRIBUTE = "path"
private const val SECURE_ATTRIBUTE = "secure"
private const val HTTP_ONLY_ATTRIBUTE = "httponly"
private const val SAME_SITE_ATTRIBUTE = "samesite"
private const val EXPIRES_ATTRIBUTE = "expires"
private const val MAX_AGE_ATTRIBUTE = "max-age"

private const val SAME_SITE_NONE = "none"
private const val SAME_SITE_LAX = "lax"
private const val SAME_SITE_STRICT = "strict"

internal data class ParsedSetCookie(
    val entity: WebViewCookieEntity,
    val isSession: Boolean,
)

// Set-Cookie 형식을 읽는다. Domain 속성이 없으면 그 주소의 호스트에만 보내는 쿠키다.
internal fun parseSetCookie(
    url: String,
    setCookie: String,
): ParsedSetCookie? {
    val partList = setCookie.split(ATTRIBUTE_SEPARATOR).map { part -> part.trim() }
    val nameValue = partList.firstOrNull()?.takeIf { part -> part.contains(VALUE_SEPARATOR) }
    val host = Uri.parse(url).host
    if (nameValue == null || host == null) return null
    val attributeMap =
        partList
            .drop(1)
            .filter { part -> part.isNotEmpty() }
            .associate { part ->
                part.substringBefore(VALUE_SEPARATOR).trim().lowercase() to part.substringAfter(VALUE_SEPARATOR, missingDelimiterValue = "").trim()
            }

    return ParsedSetCookie(
        entity =
            WebViewCookieEntity(
                name = nameValue.substringBefore(VALUE_SEPARATOR).trim(),
                value = nameValue.substringAfter(VALUE_SEPARATOR).trim(),
                domain = attributeMap[DOMAIN_ATTRIBUTE]?.let { domain -> DOMAIN_PREFIX + domain.removePrefix(DOMAIN_PREFIX) } ?: host,
                path = attributeMap[PATH_ATTRIBUTE]?.takeIf { path -> path.isNotEmpty() } ?: DEFAULT_PATH,
                isSecure = SECURE_ATTRIBUTE in attributeMap,
                isHttpOnly = HTTP_ONLY_ATTRIBUTE in attributeMap,
                sameSite =
                    when (attributeMap[SAME_SITE_ATTRIBUTE]?.lowercase()) {
                        SAME_SITE_NONE -> WebViewCookieSameSiteEntity.NONE
                        SAME_SITE_LAX -> WebViewCookieSameSiteEntity.LAX
                        SAME_SITE_STRICT -> WebViewCookieSameSiteEntity.STRICT
                        else -> WebViewCookieSameSiteEntity.UNSPECIFIED
                    },
            ),
        isSession = EXPIRES_ATTRIBUTE !in attributeMap && MAX_AGE_ATTRIBUTE !in attributeMap,
    )
}

internal fun WebViewCookieEntity.toUrl(): String = SECURE_SCHEME + domain.removePrefix(DOMAIN_PREFIX) + path

// Domain 속성을 붙이면 하위 도메인에도 보내는 쿠키가 되므로, 호스트에만 보내던 쿠키에는 붙이지 않는다.
internal fun WebViewCookieEntity.toSetCookie(): String =
    buildList {
        add("$name$VALUE_SEPARATOR$value")
        if (domain.startsWith(DOMAIN_PREFIX)) add("Domain$VALUE_SEPARATOR$domain")
        add("Path$VALUE_SEPARATOR$path")
        if (isSecure) add("Secure")
        if (isHttpOnly) add("HttpOnly")
        when (sameSite) {
            WebViewCookieSameSiteEntity.NONE -> add("SameSite${VALUE_SEPARATOR}None")
            WebViewCookieSameSiteEntity.LAX -> add("SameSite${VALUE_SEPARATOR}Lax")
            WebViewCookieSameSiteEntity.STRICT -> add("SameSite${VALUE_SEPARATOR}Strict")
            WebViewCookieSameSiteEntity.UNSPECIFIED -> Unit
        }
    }.joinToString(separator = "$ATTRIBUTE_SEPARATOR ")
