package io.github.taetae98coding.diary.data.web.mapper

import io.github.taetae98coding.diary.core.datastore.api.web.entity.WebLoginCookieLocalEntity
import io.github.taetae98coding.diary.core.datastore.api.web.entity.WebLoginCookieSameSiteLocalEntity
import io.github.taetae98coding.diary.core.datastore.api.web.entity.WebLoginLocalEntity
import io.github.taetae98coding.diary.core.model.web.WebLogin
import io.github.taetae98coding.diary.core.model.web.WebLoginCookie
import io.github.taetae98coding.diary.core.model.web.WebLoginCookieSameSite
import io.github.taetae98coding.diary.core.webcookie.api.WebViewCookieEntity
import io.github.taetae98coding.diary.core.webcookie.api.WebViewCookieSameSiteEntity
import io.github.taetae98coding.diary.core.webcookie.api.WebViewSessionCookieEntity

internal fun WebViewSessionCookieEntity.toDomain(): WebLogin =
    WebLogin(
        sessionCookieList = cookieList.map { cookie -> cookie.toDomain() },
        siteUrlSet = siteUrlSet,
    )

internal fun WebLoginLocalEntity.toDomain(): WebLogin =
    WebLogin(
        sessionCookieList = sessionCookieList.map { cookie -> cookie.toDomain() },
        siteUrlSet = siteUrlSet,
    )

internal fun WebLogin.toLocal(): WebLoginLocalEntity =
    WebLoginLocalEntity(
        sessionCookieList = sessionCookieList.map { cookie -> cookie.toLocal() },
        siteUrlSet = siteUrlSet,
    )

internal fun WebLoginCookie.toWebView(): WebViewCookieEntity =
    WebViewCookieEntity(
        name = name,
        value = value,
        domain = domain,
        path = path,
        isSecure = isSecure,
        isHttpOnly = isHttpOnly,
        sameSite =
            when (sameSite) {
                WebLoginCookieSameSite.UNSPECIFIED -> WebViewCookieSameSiteEntity.UNSPECIFIED
                WebLoginCookieSameSite.NONE -> WebViewCookieSameSiteEntity.NONE
                WebLoginCookieSameSite.LAX -> WebViewCookieSameSiteEntity.LAX
                WebLoginCookieSameSite.STRICT -> WebViewCookieSameSiteEntity.STRICT
            },
    )

private fun WebViewCookieEntity.toDomain(): WebLoginCookie =
    WebLoginCookie(
        name = name,
        value = value,
        domain = domain,
        path = path,
        isSecure = isSecure,
        isHttpOnly = isHttpOnly,
        sameSite =
            when (sameSite) {
                WebViewCookieSameSiteEntity.UNSPECIFIED -> WebLoginCookieSameSite.UNSPECIFIED
                WebViewCookieSameSiteEntity.NONE -> WebLoginCookieSameSite.NONE
                WebViewCookieSameSiteEntity.LAX -> WebLoginCookieSameSite.LAX
                WebViewCookieSameSiteEntity.STRICT -> WebLoginCookieSameSite.STRICT
            },
    )

private fun WebLoginCookieLocalEntity.toDomain(): WebLoginCookie =
    WebLoginCookie(
        name = name,
        value = value,
        domain = domain,
        path = path,
        isSecure = isSecure,
        isHttpOnly = isHttpOnly,
        sameSite =
            when (sameSite) {
                WebLoginCookieSameSiteLocalEntity.UNSPECIFIED -> WebLoginCookieSameSite.UNSPECIFIED
                WebLoginCookieSameSiteLocalEntity.NONE -> WebLoginCookieSameSite.NONE
                WebLoginCookieSameSiteLocalEntity.LAX -> WebLoginCookieSameSite.LAX
                WebLoginCookieSameSiteLocalEntity.STRICT -> WebLoginCookieSameSite.STRICT
            },
    )

private fun WebLoginCookie.toLocal(): WebLoginCookieLocalEntity =
    WebLoginCookieLocalEntity(
        name = name,
        value = value,
        domain = domain,
        path = path,
        isSecure = isSecure,
        isHttpOnly = isHttpOnly,
        sameSite =
            when (sameSite) {
                WebLoginCookieSameSite.UNSPECIFIED -> WebLoginCookieSameSiteLocalEntity.UNSPECIFIED
                WebLoginCookieSameSite.NONE -> WebLoginCookieSameSiteLocalEntity.NONE
                WebLoginCookieSameSite.LAX -> WebLoginCookieSameSiteLocalEntity.LAX
                WebLoginCookieSameSite.STRICT -> WebLoginCookieSameSiteLocalEntity.STRICT
            },
    )
