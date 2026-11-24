package io.github.taetae98coding.diary.core.webcookie.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.webcookie.api.WebViewCookieDataSource
import io.github.taetae98coding.diary.core.webcookie.api.WebViewCookieEntity
import io.github.taetae98coding.diary.core.webcookie.api.WebViewCookieSameSiteEntity
import io.github.taetae98coding.diary.core.webcookie.api.WebViewSessionCookieEntity
import io.github.taetae98coding.diary.library.webkit.WebKitCookie
import io.github.taetae98coding.diary.library.webkit.WebKitCookieSameSite
import io.github.taetae98coding.diary.library.webkit.WebKitCookieStore

@Inject
@ContributesBinding(AppScope::class)
internal class JvmWebViewCookieDataSource : WebViewCookieDataSource {
    // WKWebView의 쿠키 보관 공간은 모든 쿠키를 꺼낼 수 있어 주소를 따로 확인하지 않는다.
    override suspend fun readSessionCookie(siteUrlSet: Set<String>): WebViewSessionCookieEntity =
        WebViewSessionCookieEntity(
            cookieList = WebKitCookieStore.readSessionCookies().map { cookie -> cookie.toEntity() },
            siteUrlSet = emptySet(),
        )

    override suspend fun upsertCookie(cookieList: List<WebViewCookieEntity>) {
        WebKitCookieStore.setCookies(cookieList = cookieList.map { cookie -> cookie.toWebKitCookie() })
    }

    // WKWebView는 쿠키 보관 공간을 스스로 기기에 기록한다.
    override suspend fun flush(): Unit = Unit
}

internal fun WebKitCookie.toEntity(): WebViewCookieEntity =
    WebViewCookieEntity(
        name = name,
        value = value,
        domain = domain,
        path = path,
        isSecure = isSecure,
        isHttpOnly = isHttpOnly,
        sameSite =
            when (sameSite) {
                WebKitCookieSameSite.UNSPECIFIED -> WebViewCookieSameSiteEntity.UNSPECIFIED
                WebKitCookieSameSite.NONE -> WebViewCookieSameSiteEntity.NONE
                WebKitCookieSameSite.LAX -> WebViewCookieSameSiteEntity.LAX
                WebKitCookieSameSite.STRICT -> WebViewCookieSameSiteEntity.STRICT
            },
    )

internal fun WebViewCookieEntity.toWebKitCookie(): WebKitCookie =
    WebKitCookie(
        name = name,
        value = value,
        domain = domain,
        path = path,
        expiresAtEpochMilliseconds = null,
        isSecure = isSecure,
        isHttpOnly = isHttpOnly,
        sameSite =
            when (sameSite) {
                WebViewCookieSameSiteEntity.UNSPECIFIED -> WebKitCookieSameSite.UNSPECIFIED
                WebViewCookieSameSiteEntity.NONE -> WebKitCookieSameSite.NONE
                WebViewCookieSameSiteEntity.LAX -> WebKitCookieSameSite.LAX
                WebViewCookieSameSiteEntity.STRICT -> WebKitCookieSameSite.STRICT
            },
    )
