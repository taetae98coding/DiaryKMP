package io.github.taetae98coding.diary.core.webcookie.impl

import android.webkit.CookieManager
import androidx.webkit.CookieManagerCompat
import androidx.webkit.WebViewFeature
import io.github.taetae98coding.diary.core.webcookie.api.WebViewCookieDataSource
import io.github.taetae98coding.diary.core.webcookie.api.WebViewCookieEntity
import io.github.taetae98coding.diary.core.webcookie.api.WebViewSessionCookieEntity
import io.github.taetae98coding.diary.core.webcookie.api.WebViewVisitedUrlRegistry
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Factory
import kotlin.coroutines.resume

// CookieManager는 쿠키를 넣은 결과를 부른 스레드의 Looper로 돌려주므로, 앱 화면이 메인 스레드에서 부르는 것을 전제로 스레드를 옮기지 않는다.
@Factory
internal class AndroidWebViewCookieDataSource(
    private val cookieManager: CookieManager,
) : WebViewCookieDataSource {
    // Android WebView는 쿠키 목록 전체를 꺼낼 수 없고, 주소 하나에 보내는 쿠키만 속성과 함께 읽을 수 있다.
    override suspend fun readSessionCookie(siteUrlSet: Set<String>): WebViewSessionCookieEntity? {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.GET_COOKIE_INFO)) return null

        val cookieListByUrl =
            (siteUrlSet + WebViewVisitedUrlRegistry.read()).associateWith { url ->
                CookieManagerCompat
                    .getCookieInfo(cookieManager, url)
                    .mapNotNull { setCookie -> parseSetCookie(url = url, setCookie = setCookie) }
                    .filter { cookie -> cookie.isSession }
                    .map { cookie -> cookie.entity }
            }

        return WebViewSessionCookieEntity(
            cookieList =
                cookieListByUrl.values
                    .flatten()
                    .distinctBy { cookie -> Triple(cookie.name, cookie.domain, cookie.path) },
            siteUrlSet = cookieListByUrl.filterValues { cookieList -> cookieList.isNotEmpty() }.keys,
        )
    }

    override suspend fun upsertCookie(cookieList: List<WebViewCookieEntity>) {
        cookieList.forEach { cookie ->
            suspendCancellableCoroutine { continuation ->
                cookieManager.setCookie(cookie.toUrl(), cookie.toSetCookie()) { continuation.resume(Unit) }
            }
        }
    }

    override suspend fun flush() {
        cookieManager.flush()
    }
}
