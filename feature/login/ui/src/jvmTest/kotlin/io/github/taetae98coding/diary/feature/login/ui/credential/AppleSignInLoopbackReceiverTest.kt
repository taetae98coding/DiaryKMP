package io.github.taetae98coding.diary.feature.login.ui.credential

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldStartWith
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Locale

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class AppleSignInLoopbackReceiverTest :
    FunSpec({
        test("TC-LOGIN-DATA-013 로컬 수신 주소로 돌아온 idToken과 상태 값을 이번 시도의 응답으로 받고 결과 페이지를 표시한다") {
            val idToken = "id-token-${fixtureMonkey.giveMeOne<String>()}"
            val state =
                AppleWebSignInState.encode(
                    token = fixtureMonkey.giveMeOne<String>(),
                    returnUri = "http://127.0.0.1:${fixtureMonkey.giveMeOne<Int>().toUInt()}/callback",
                )

            AppleSignInLoopbackReceiver(locale = Locale.KOREAN).use { receiver ->
                receiver.returnUri shouldStartWith "http://127.0.0.1:"

                val page = get("${receiver.returnUri}?id_token=${idToken.urlEncode()}&state=${state.urlEncode()}")
                val response = receiver.waitForResponse()

                page.statusCode() shouldBe 200
                page.body() shouldContain "이 창을 닫고 앱으로 돌아가세요."
                page.body() shouldNotContain idToken
                response shouldBe AppleWebSignInResponse(idToken = idToken, state = state, error = null)
            }
        }

        test("오류 응답도 그대로 받고, 한국어가 아니면 기본 문구를 표시한다") {
            val state = "state-${fixtureMonkey.giveMeOne<String>()}"

            AppleSignInLoopbackReceiver(locale = Locale.ENGLISH).use { receiver ->
                val page = get("${receiver.returnUri}?error=user_cancelled_authorize&state=${state.urlEncode()}")
                val response = receiver.waitForResponse()

                page.body() shouldContain "You can close this window and return to the app."
                response shouldBe AppleWebSignInResponse(idToken = null, state = state, error = "user_cancelled_authorize")
            }
        }
    })

private fun get(url: String): HttpResponse<String> =
    HttpClient
        .newHttpClient()
        .send(HttpRequest.newBuilder(URI(url)).GET().build(), HttpResponse.BodyHandlers.ofString())

private fun String.urlEncode(): String = URLEncoder.encode(this, Charsets.UTF_8)
