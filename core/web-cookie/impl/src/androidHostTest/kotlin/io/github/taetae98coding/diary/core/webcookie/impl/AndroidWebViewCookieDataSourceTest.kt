package io.github.taetae98coding.diary.core.webcookie.impl

import android.webkit.CookieManager
import androidx.webkit.CookieManagerCompat
import androidx.webkit.WebViewFeature
import io.github.taetae98coding.diary.core.webcookie.api.WebViewCookieEntity
import io.github.taetae98coding.diary.core.webcookie.api.WebViewCookieSameSiteEntity
import io.github.taetae98coding.diary.core.webcookie.api.WebViewSessionCookieEntity
import io.github.taetae98coding.diary.core.webcookie.api.WebViewVisitedUrlRegistry
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

// 쿠키의 이름과 사이트가 판정 대상이고, 직접 연 주소 목록은 앱 전체에 하나라 테스트마다 다른 사이트를 쓴다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AndroidWebViewCookieDataSourceTest {
    private val cookieManager = mockk<CookieManager>(relaxed = true)
    private val dataSource = AndroidWebViewCookieDataSource(cookieManager = cookieManager)

    @Before
    fun setUp() {
        mockkStatic(WebViewFeature::class, CookieManagerCompat::class)
        every { WebViewFeature.isFeatureSupported(WebViewFeature.GET_COOKIE_INFO) } returns true
        every { CookieManagerCompat.getCookieInfo(cookieManager, any()) } returns emptyList()
    }

    @After
    fun tearDown() {
        unmockkStatic(WebViewFeature::class, CookieManagerCompat::class)
    }

    @Test
    fun `TC-4f4af7a4-f3ec-45bc-9a06-36ba0812e50d 직접 연 페이지의 주소에 해당하는 세션 쿠키만 보관한다`() {
        val visitedHost = uniqueHost()
        val otherHost = uniqueHost()
        val visitedUrl = url(host = visitedHost, path = "/page")
        WebViewVisitedUrlRegistry.record(url = visitedUrl)
        every { CookieManagerCompat.getCookieInfo(cookieManager, visitedUrl) } returns listOf("sid=visited; Path=/; Secure; HttpOnly; SameSite=Lax")
        every { CookieManagerCompat.getCookieInfo(cookieManager, url(host = otherHost, path = "/")) } returns listOf("sid=other; Path=/")

        val result = runBlocking { dataSource.readSessionCookie(siteUrlSet = emptySet()) }

        result shouldBe
            WebViewSessionCookieEntity(
                cookieList =
                    listOf(
                        WebViewCookieEntity(
                            name = "sid",
                            value = "visited",
                            domain = visitedHost,
                            path = "/",
                            isSecure = true,
                            isHttpOnly = true,
                            sameSite = WebViewCookieSameSiteEntity.LAX,
                        ),
                    ),
                siteUrlSet = setOf(visitedUrl),
            )
    }

    @Test
    fun `TC-5c2c51e7-49c1-4fd7-9f84-77c0426380ed 이전에 보관한 사이트의 주소에서도 세션 쿠키를 확인한다`() {
        val savedUrl = uniqueUrl()
        every { CookieManagerCompat.getCookieInfo(cookieManager, savedUrl) } returns listOf("sid=saved; Domain=example.com; Path=/")

        val result = runBlocking { dataSource.readSessionCookie(siteUrlSet = setOf(savedUrl)) }

        result?.cookieList?.map { cookie -> cookie.value } shouldBe listOf("saved")
        result?.cookieList?.single()?.domain shouldBe ".example.com"
        result?.siteUrlSet shouldBe setOf(savedUrl)
    }

    @Test
    fun `TC-e5c05f67-9b54-471d-9500-4976ff8cb13e 세션 쿠키가 남지 않은 사이트의 주소는 보관하지 않는다`() {
        val emptyUrl = uniqueUrl()
        val persistentUrl = uniqueUrl()
        WebViewVisitedUrlRegistry.record(url = emptyUrl)
        WebViewVisitedUrlRegistry.record(url = persistentUrl)
        every { CookieManagerCompat.getCookieInfo(cookieManager, persistentUrl) } returns
            listOf("expires=1; Path=/; Expires=Wed, 21 Oct 2099 07:28:00 GMT", "maxAge=1; Path=/; Max-Age=3600")

        val result = runBlocking { dataSource.readSessionCookie(siteUrlSet = emptySet()) }

        result?.siteUrlSet?.contains(emptyUrl) shouldBe false
        result?.siteUrlSet?.contains(persistentUrl) shouldBe false
        result?.cookieList?.any { cookie -> cookie.name == "expires" || cookie.name == "maxAge" } shouldBe false
    }

    @Test
    fun `TC-5af3c19f-698d-4340-bade-b68e163a5da9 세션 쿠키를 확인하는 기능이 없으면 확인할 수 없다고 알린다`() {
        every { WebViewFeature.isFeatureSupported(WebViewFeature.GET_COOKIE_INFO) } returns false
        WebViewVisitedUrlRegistry.record(url = uniqueUrl())

        runBlocking { dataSource.readSessionCookie(siteUrlSet = emptySet()) }.shouldBeNull()
    }

    @Test
    fun `TC-f2f9cbff-6f3d-47fe-a8cd-e5e8a527031e 웹 표시 수단의 로그인 정보를 기기에 바로 기록한다`() {
        runBlocking { dataSource.flush() }

        verify(exactly = 1) { cookieManager.flush() }
    }

    @Test
    fun `같은 쿠키가 여러 주소에서 나오면 하나로 보관한다`() {
        val host = uniqueHost()
        val firstUrl = url(host = host, path = "/first")
        val secondUrl = url(host = host, path = "/second")
        every { CookieManagerCompat.getCookieInfo(cookieManager, firstUrl) } returns listOf("sid=1; Path=/")
        every { CookieManagerCompat.getCookieInfo(cookieManager, secondUrl) } returns listOf("sid=1; Path=/")

        val result = runBlocking { dataSource.readSessionCookie(siteUrlSet = setOf(firstUrl, secondUrl)) }

        result?.cookieList?.size shouldBe 1
        result?.siteUrlSet shouldBe setOf(firstUrl, secondUrl)
    }

    private fun uniqueHost(): String = "${Uuid.random().toHexString()}.example.com"

    private fun uniqueUrl(): String = url(host = uniqueHost(), path = "/page")

    private fun url(
        host: String,
        path: String,
    ): String = "https://$host$path"
}
