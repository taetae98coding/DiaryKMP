package io.github.taetae98coding.diary.core.webcookie.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.webcookie.api.WebViewCookieDataSource
import io.github.taetae98coding.diary.core.webcookie.api.WebViewCookieEntity
import io.github.taetae98coding.diary.core.webcookie.api.WebViewSessionCookieEntity

// 웹의 웹 표시 수단은 브라우저 안에 다른 사이트를 끼워 넣어, 앱이 그 사이트의 쿠키를 읽거나 쓸 수 없다.
@Inject
@ContributesBinding(AppScope::class)
internal class WasmWebViewCookieDataSource : WebViewCookieDataSource {
    override suspend fun readSessionCookie(siteUrlSet: Set<String>): WebViewSessionCookieEntity? = null

    override suspend fun upsertCookie(cookieList: List<WebViewCookieEntity>): Unit = Unit

    override suspend fun flush(): Unit = Unit
}
