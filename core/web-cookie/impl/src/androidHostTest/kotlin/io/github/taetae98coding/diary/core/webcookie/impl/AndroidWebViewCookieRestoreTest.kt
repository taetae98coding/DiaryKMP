package io.github.taetae98coding.diary.core.webcookie.impl

import android.webkit.CookieManager
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.webcookie.api.WebViewCookieEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

// Set-Cookie 형식에 쓰이는 구분 문자가 이름과 값에 섞이면 다른 쿠키가 되므로, 이름과 값만 영문자로 고정한다.
private fun testCookie(): WebViewCookieEntity =
    fixtureMonkey.giveMeOne<WebViewCookieEntity>().copy(
        name = "name${Uuid.random().toHexString()}",
        value = "value${Uuid.random().toHexString()}",
    )

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AndroidWebViewCookieRestoreTest {
    @Test
    fun `TC-a8c81333-f7af-4735-86ec-9a6a1a6e8c6d 되돌릴 때 같은 쿠키가 이미 있으면 보관해 둔 값으로 바꾼다`() {
        val cookieManager = CookieManager.getInstance()
        val cookie =
            fixtureMonkey.giveMeOne<WebViewCookieEntity>().copy(
                name = "sid",
                value = "saved",
                domain = "${Uuid.random().toHexString()}.example.com",
                path = "/",
                isSecure = false,
            )
        cookieManager.setCookie(cookie.toUrl(), "sid=old; Path=/")

        runBlocking { AndroidWebViewCookieDataSource(cookieManager = cookieManager).upsertCookie(cookieList = listOf(cookie)) }

        cookieManager.getCookie(cookie.toUrl()) shouldBe "sid=saved"
    }

    @Test
    fun `TC-8deae54a-4c48-42e5-ae00-37a42552acb5 보관해 둔 정보를 모두 담아 웹 표시 수단에 넣는다`() {
        val cookie = testCookie().copy(domain = ".example.com", path = "/inbox")

        cookie.toUrl() shouldBe "https://example.com/inbox"
        parseSetCookie(url = cookie.toUrl(), setCookie = cookie.toSetCookie()) shouldBe ParsedSetCookie(entity = cookie, isSession = true)
    }

    @Test
    fun `호스트에만 보내던 쿠키는 되돌려도 호스트에만 보낸다`() {
        val cookie = testCookie().copy(domain = "host.example.com", path = "/")

        parseSetCookie(url = cookie.toUrl(), setCookie = cookie.toSetCookie()) shouldBe ParsedSetCookie(entity = cookie, isSession = true)
    }
}
