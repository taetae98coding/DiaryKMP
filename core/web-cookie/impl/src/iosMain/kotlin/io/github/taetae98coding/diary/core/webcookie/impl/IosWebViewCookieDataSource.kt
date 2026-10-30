package io.github.taetae98coding.diary.core.webcookie.impl

import io.github.taetae98coding.diary.core.webcookie.api.WebViewCookieDataSource
import io.github.taetae98coding.diary.core.webcookie.api.WebViewCookieEntity
import io.github.taetae98coding.diary.core.webcookie.api.WebViewCookieSameSiteEntity
import io.github.taetae98coding.diary.core.webcookie.api.WebViewSessionCookieEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import platform.Foundation.NSHTTPCookie
import platform.Foundation.NSHTTPCookieDomain
import platform.Foundation.NSHTTPCookieName
import platform.Foundation.NSHTTPCookiePath
import platform.Foundation.NSHTTPCookieSameSiteLax
import platform.Foundation.NSHTTPCookieSameSitePolicy
import platform.Foundation.NSHTTPCookieSameSiteStrict
import platform.Foundation.NSHTTPCookieSecure
import platform.Foundation.NSHTTPCookieValue
import platform.WebKit.WKHTTPCookieStore
import platform.WebKit.WKWebsiteDataStore
import kotlin.coroutines.resume

// NSHTTPCookie는 HttpOnly 속성의 공개 상수가 없고 이 키 문자열만 받는다.
private const val HTTP_ONLY_KEY = "HttpOnly"
private const val TRUE_VALUE = "TRUE"

// WKHTTPCookieStore는 메인 스레드에서만 다룬다.
@Factory
internal class IosWebViewCookieDataSource : WebViewCookieDataSource {
    // WKWebView의 쿠키 보관 공간은 모든 쿠키를 꺼낼 수 있어 주소를 따로 확인하지 않는다.
    override suspend fun readSessionCookie(siteUrlSet: Set<String>): WebViewSessionCookieEntity =
        withContext(Dispatchers.Main) {
            val cookieList =
                suspendCancellableCoroutine { continuation ->
                    cookieStore().getAllCookies { cookies ->
                        continuation.resume(cookies.orEmpty().filterIsInstance<NSHTTPCookie>())
                    }
                }

            WebViewSessionCookieEntity(
                cookieList = cookieList.filter { cookie -> cookie.sessionOnly }.map { cookie -> cookie.toEntity() },
                siteUrlSet = emptySet(),
            )
        }

    override suspend fun upsertCookie(cookieList: List<WebViewCookieEntity>) {
        withContext(Dispatchers.Main) {
            val store = cookieStore()

            cookieList.mapNotNull { cookie -> cookie.toNsHttpCookie() }.forEach { cookie ->
                suspendCancellableCoroutine { continuation ->
                    store.setCookie(cookie) { continuation.resume(Unit) }
                }
            }
        }
    }

    // WKWebView는 쿠키 보관 공간을 스스로 기기에 기록한다.
    override suspend fun flush(): Unit = Unit
}

private fun cookieStore(): WKHTTPCookieStore = WKWebsiteDataStore.defaultDataStore().httpCookieStore

private fun NSHTTPCookie.toEntity(): WebViewCookieEntity =
    WebViewCookieEntity(
        name = name,
        value = value,
        domain = domain,
        path = path,
        isSecure = secure,
        isHttpOnly = HTTPOnly,
        sameSite =
            when (sameSitePolicy) {
                NSHTTPCookieSameSiteLax -> WebViewCookieSameSiteEntity.LAX
                NSHTTPCookieSameSiteStrict -> WebViewCookieSameSiteEntity.STRICT
                else -> WebViewCookieSameSiteEntity.UNSPECIFIED
            },
    )

private fun WebViewCookieEntity.toNsHttpCookie(): NSHTTPCookie? {
    val properties =
        buildMap<Any?, Any?> {
            put(NSHTTPCookieName, name)
            put(NSHTTPCookieValue, value)
            put(NSHTTPCookieDomain, domain)
            put(NSHTTPCookiePath, path)
            if (isSecure) put(NSHTTPCookieSecure, TRUE_VALUE)
            if (isHttpOnly) put(HTTP_ONLY_KEY, TRUE_VALUE)
            when (sameSite) {
                WebViewCookieSameSiteEntity.LAX -> put(NSHTTPCookieSameSitePolicy, NSHTTPCookieSameSiteLax)
                WebViewCookieSameSiteEntity.STRICT -> put(NSHTTPCookieSameSitePolicy, NSHTTPCookieSameSiteStrict)
                WebViewCookieSameSiteEntity.UNSPECIFIED, WebViewCookieSameSiteEntity.NONE -> Unit
            }
        }

    return NSHTTPCookie.cookieWithProperties(properties)
}
