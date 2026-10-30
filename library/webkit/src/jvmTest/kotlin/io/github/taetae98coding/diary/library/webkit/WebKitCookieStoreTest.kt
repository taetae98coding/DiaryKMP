package io.github.taetae98coding.diary.library.webkit

import io.github.taetae98coding.diary.library.objc.ObjCFramework
import io.github.taetae98coding.diary.library.objc.ObjCRuntime
import io.github.taetae98coding.diary.library.objc.send
import io.github.taetae98coding.diary.library.objc.sendBoolean
import io.github.taetae98coding.diary.library.objc.sendDouble
import io.github.taetae98coding.diary.library.objc.sendVoid
import io.github.taetae98coding.diary.library.objc.utf8String
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.lang.foreign.MemorySegment

private const val MILLIS_PER_SECOND = 1_000.0
private const val EXPIRES_AT_EPOCH_MILLISECONDS = 1_800_000_000_000L

// 쿠키 속성이 NSHTTPCookie에 그대로 전달되는지가 검증 대상이라 임의 생성 대신 대표 고정 값을 쓴다.
class WebKitCookieStoreTest :
    FunSpec({
        test("TC-8deae54a-4c48-42e5-ae00-37a42552acb5 이름, 값, 도메인, 경로, 만료 시각을 NSHTTPCookie로 옮긴다") {
            ObjCRuntime.withAutoreleasePool {
                val cookie =
                    WebKitCookie(
                        name = "session",
                        value = "abc=123",
                        domain = ".example.com",
                        path = "/inbox",
                        expiresAtEpochMilliseconds = EXPIRES_AT_EPOCH_MILLISECONDS,
                        isSecure = true,
                        isHttpOnly = true,
                        sameSite = WebKitCookieSameSite.LAX,
                    ).toNsHttpCookie()

                cookie.shouldNotBeNull()
                cookie.send(ObjCRuntime.selector("name")).utf8String() shouldBe "session"
                cookie.send(ObjCRuntime.selector("value")).utf8String() shouldBe "abc=123"
                cookie.send(ObjCRuntime.selector("domain")).utf8String().removePrefix(".") shouldBe "example.com"
                cookie.send(ObjCRuntime.selector("path")).utf8String() shouldBe "/inbox"
                cookie
                    .send(ObjCRuntime.selector("expiresDate"))
                    .sendDouble(ObjCRuntime.selector("timeIntervalSince1970")) shouldBe
                    (EXPIRES_AT_EPOCH_MILLISECONDS / MILLIS_PER_SECOND plusOrMinus 1.0)
            }
        }

        test("TC-8deae54a-4c48-42e5-ae00-37a42552acb5 보안 연결 전용, 스크립트 접근 금지, 사이트 간 전송 정책을 NSHTTPCookie로 옮긴다") {
            ObjCRuntime.withAutoreleasePool {
                val cases =
                    listOf(
                        Triple(true, true, WebKitCookieSameSite.LAX),
                        Triple(false, false, WebKitCookieSameSite.STRICT),
                        Triple(true, false, WebKitCookieSameSite.NONE),
                        Triple(false, true, WebKitCookieSameSite.UNSPECIFIED),
                    )

                cases.forEach { (isSecure, isHttpOnly, sameSite) ->
                    val cookie =
                        WebKitCookie(
                            name = "flag",
                            value = "1",
                            domain = ".example.com",
                            path = "/",
                            expiresAtEpochMilliseconds = null,
                            isSecure = isSecure,
                            isHttpOnly = isHttpOnly,
                            sameSite = sameSite,
                        ).toNsHttpCookie()

                    cookie.shouldNotBeNull()
                    cookie.sendBoolean(ObjCRuntime.selector("isSecure")) shouldBe isSecure
                    cookie.sendBoolean(ObjCRuntime.selector("isHTTPOnly")) shouldBe isHttpOnly
                    cookie.send(ObjCRuntime.selector("expiresDate")).address() shouldBe 0L

                    val policy = cookie.send(ObjCRuntime.selector("sameSitePolicy"))
                    val expectedPolicy = expectedSameSitePolicyMap[sameSite]

                    if (expectedPolicy == null) {
                        policy.address() shouldBe 0L
                    } else {
                        policy.utf8String() shouldBe foundationString(expectedPolicy).utf8String()
                    }
                }
            }
        }

        test("TC-908e16b2-fd82-4d9d-83b8-9d6431d795a4 쿠키 목록에서 세션 쿠키만 모든 정보와 함께 읽는다") {
            ObjCRuntime.withAutoreleasePool {
                val sessionCookieList =
                    listOf(
                        sessionCookie(name = "lax", isSecure = true, isHttpOnly = true, sameSite = WebKitCookieSameSite.LAX),
                        sessionCookie(name = "strict", isSecure = false, isHttpOnly = false, sameSite = WebKitCookieSameSite.STRICT),
                        sessionCookie(name = "unspecified", isSecure = true, isHttpOnly = false, sameSite = WebKitCookieSameSite.UNSPECIFIED),
                    )
                val persistentCookie =
                    sessionCookie(name = "persistent", isSecure = false, isHttpOnly = true, sameSite = WebKitCookieSameSite.LAX)
                        .copy(expiresAtEpochMilliseconds = EXPIRES_AT_EPOCH_MILLISECONDS)
                val array = ObjCRuntime.objcClass("NSMutableArray").send(ObjCRuntime.selector("array"))

                (sessionCookieList + persistentCookie).forEach { cookie ->
                    array.sendVoid(ObjCRuntime.selector("addObject:"), cookie.toNsHttpCookie().shouldNotBeNull())
                }

                array.toSessionCookieList().map { cookie -> cookie.copy(domain = cookie.domain.removePrefix(".")) } shouldBe
                    sessionCookieList.map { cookie -> cookie.copy(domain = cookie.domain.removePrefix(".")) }
            }
        }
    })

private fun sessionCookie(
    name: String,
    isSecure: Boolean,
    isHttpOnly: Boolean,
    sameSite: WebKitCookieSameSite,
): WebKitCookie =
    WebKitCookie(
        name = name,
        value = "value-$name",
        domain = ".example.com",
        path = "/$name",
        expiresAtEpochMilliseconds = null,
        isSecure = isSecure,
        isHttpOnly = isHttpOnly,
        sameSite = sameSite,
    )

// jqwik 엔진은 enum when이 만드는 WhenMappings 합성 클래스를 탐색하다 실패하므로 매핑을 표로 둔다.
private val expectedSameSitePolicyMap: Map<WebKitCookieSameSite, String?> =
    mapOf(
        WebKitCookieSameSite.LAX to "NSHTTPCookieSameSiteLax",
        WebKitCookieSameSite.STRICT to "NSHTTPCookieSameSiteStrict",
        WebKitCookieSameSite.NONE to null,
        WebKitCookieSameSite.UNSPECIFIED to null,
    )

private fun foundationString(name: String): MemorySegment = ObjCFramework.string(frameworkPath = ObjCFramework.FOUNDATION_PATH, name = name)
