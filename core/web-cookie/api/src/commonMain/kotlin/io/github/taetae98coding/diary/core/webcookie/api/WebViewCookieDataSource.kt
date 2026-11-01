package io.github.taetae98coding.diary.core.webcookie.api

public interface WebViewCookieDataSource {
    /**
     * 웹 표시 수단의 세션 쿠키를 읽는다. 세션 쿠키를 주소마다만 확인할 수 있는 웹 표시 수단은 [siteUrlSet]과 이번 실행에서 직접 연
     * 주소에서 확인하고, 세션 쿠키가 나온 주소를 함께 돌려준다. 세션 쿠키를 확인할 수 없는 웹 표시 수단이면 `null`이다.
     */
    public suspend fun readSessionCookie(siteUrlSet: Set<String>): WebViewSessionCookieEntity?

    public suspend fun upsertCookie(cookieList: List<WebViewCookieEntity>)

    public suspend fun flush()
}
