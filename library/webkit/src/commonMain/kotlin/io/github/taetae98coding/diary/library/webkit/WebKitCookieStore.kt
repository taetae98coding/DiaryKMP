package io.github.taetae98coding.diary.library.webkit

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val FOUNDATION_PATH = "/System/Library/Frameworks/Foundation.framework/Foundation"

// NSHTTPCookie는 HttpOnly 속성의 공개 상수가 없고 이 키 문자열만 받는다.
private const val HTTP_ONLY_KEY = "HttpOnly"
private const val TRUE_VALUE = "TRUE"
private const val MILLIS_PER_SECOND = 1_000.0

/**
 * 기본 WKWebViewConfiguration이 쓰는 WKWebsiteDataStore의 쿠키 저장소에 쿠키를 넣는다.
 * [WebKitWebViewPanel]이 만드는 웹뷰가 같은 저장소를 쓰므로 여기에 넣은 쿠키가 그 웹뷰의 요청에 실린다.
 */
public object WebKitCookieStore {
    public suspend fun setCookies(cookieList: List<WebKitCookie>) {
        if (cookieList.isEmpty()) return

        suspendCancellableCoroutine { continuation ->
            ObjCRuntime.performOnMainThread { setCookiesOnMainThread(cookieList = cookieList, continuation = continuation) }
        }
    }

    private fun setCookiesOnMainThread(
        cookieList: List<WebKitCookie>,
        continuation: CancellableContinuation<Unit>,
    ) {
        try {
            val store =
                ObjCRuntime
                    .objcClass("WKWebsiteDataStore")
                    .send(ObjCRuntime.selector("defaultDataStore"))
                    .send(ObjCRuntime.selector("httpCookieStore"))
            val nsCookieList = cookieList.mapNotNull { cookie -> cookie.toNsHttpCookie() }

            if (nsCookieList.isEmpty()) {
                continuation.resume(Unit)
                return
            }

            // 저장소는 요청을 순서대로 처리하므로 마지막 쿠키의 completion만 기다리면 앞의 쿠키도 반영되어 있다.
            nsCookieList.forEachIndexed { index, nsCookie ->
                val completion = completionHandler(isLast = index == nsCookieList.lastIndex, continuation = continuation)

                store.sendVoid(ObjCRuntime.selector("setCookie:completionHandler:"), nsCookie, completion)
            }
        } catch (throwable: Throwable) {
            if (continuation.isActive) continuation.resumeWithException(throwable)
        }
    }

    private fun completionHandler(
        isLast: Boolean,
        continuation: CancellableContinuation<Unit>,
    ): MemorySegment =
        if (isLast) {
            ObjCBlock.create { if (continuation.isActive) continuation.resume(Unit) }
        } else {
            MemorySegment.NULL
        }
}

// cookieWithProperties:는 autorelease된 객체를 반환하므로 withAutoreleasePool 안에서 쓴다.
internal fun WebKitCookie.toNsHttpCookie(): MemorySegment? {
    val properties = ObjCRuntime.objcClass("NSMutableDictionary").send(ObjCRuntime.selector("dictionary"))

    properties.setProperty(foundationString("NSHTTPCookieName"), nsString(name))
    properties.setProperty(foundationString("NSHTTPCookieValue"), nsString(value))
    properties.setProperty(foundationString("NSHTTPCookieDomain"), nsString(domain))
    properties.setProperty(foundationString("NSHTTPCookiePath"), nsString(path))

    expiresAtEpochMilliseconds?.let { millis ->
        val date =
            ObjCRuntime
                .objcClass("NSDate")
                .send(ObjCRuntime.selector("dateWithTimeIntervalSince1970:"), millis / MILLIS_PER_SECOND)

        properties.setProperty(foundationString("NSHTTPCookieExpires"), date)
    }

    if (isSecure) {
        properties.setProperty(foundationString("NSHTTPCookieSecure"), nsString(TRUE_VALUE))
    }

    if (isHttpOnly) {
        properties.setProperty(nsString(HTTP_ONLY_KEY), nsString(TRUE_VALUE))
    }

    when (sameSite) {
        WebKitCookieSameSite.LAX -> properties.setProperty(foundationString("NSHTTPCookieSameSitePolicy"), foundationString("NSHTTPCookieSameSiteLax"))
        WebKitCookieSameSite.STRICT -> properties.setProperty(foundationString("NSHTTPCookieSameSitePolicy"), foundationString("NSHTTPCookieSameSiteStrict"))
        WebKitCookieSameSite.UNSPECIFIED, WebKitCookieSameSite.NONE -> Unit
    }

    val cookie = ObjCRuntime.objcClass("NSHTTPCookie").send(ObjCRuntime.selector("cookieWithProperties:"), properties)

    return cookie.takeIf { segment -> segment.address() != 0L }
}

private fun MemorySegment.setProperty(
    key: MemorySegment,
    value: MemorySegment,
) {
    sendVoid(ObjCRuntime.selector("setObject:forKey:"), value, key)
}

// Foundation이 내보내는 NSString 상수는 포인터 변수라 한 번 더 읽어야 객체가 나온다.
private fun foundationString(name: String): MemorySegment =
    ObjCRuntime
        .frameworkSymbol(frameworkPath = FOUNDATION_PATH, name = name)
        .reinterpret(ValueLayout.ADDRESS.byteSize())
        .get(ValueLayout.ADDRESS, 0L)
