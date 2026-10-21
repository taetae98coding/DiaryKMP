package io.github.taetae98coding.diary.feature.login.ui.credential

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.library.fixturemonkey.nonBlankString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldStartWith
import java.net.ConnectException
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Locale

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class LoopbackRedirectReceiverTest :
    FunSpec({
        test("TC-LOGIN-DATA-013 로컬 수신 주소로 돌아온 idToken과 이번 시도의 상태 값을 이번 시도의 인증 결과로 받고 결과 페이지를 표시한다") {
            val idToken = "id-token-${fixtureMonkey.nonBlankString()}"

            LoopbackRedirectReceiver(locale = Locale.KOREAN).use { receiver ->
                receiver.redirectUri shouldStartWith "http://127.0.0.1:"

                val request = AppleWebSignInRequestFactory(config = fixtureMonkey.appleCredentialsConfig()).create(returnUri = receiver.redirectUri)
                val page = get("${receiver.redirectUri}?id_token=${idToken.urlEncode()}&state=${request.state.urlEncode()}")
                val credential = request.toCredential(response = AppleWebSignInResponse.fromParameters(receiver.waitForParameters()::get))

                page.statusCode() shouldBe 200
                page.body() shouldContain "이 창을 닫고 앱으로 돌아가세요."
                page.body() shouldNotContain idToken
                credential.idToken shouldBe idToken
                CredentialsNonce.hash(credential.nonce) shouldBe request.authorizationUrl.queryParameter("nonce")
            }
        }

        test("TC-LOGIN-DATA-016 로컬 수신 주소로 돌아온 Google 인가 코드와 이번 시도의 코드 확인 값, 수신 주소를 인증 결과로 받고 결과 페이지를 표시한다") {
            val code = "code-${fixtureMonkey.nonBlankString()}"
            val clientId = "${fixtureMonkey.giveMeOne<Long>().toULong()}.apps.googleusercontent.com"

            LoopbackRedirectReceiver(locale = Locale.KOREAN).use { receiver ->
                val attempt = GoogleAuthorizationCodeAttemptFactory(clientId = clientId).create(redirectUri = receiver.redirectUri)
                val codeChallenge = attempt.authorizationUrl.queryParameter("code_challenge")

                val page = get("${receiver.redirectUri}?code=${code.urlEncode()}&scope=openid")
                val credential = attempt.toCredential(parameters = receiver.waitForParameters())

                page.statusCode() shouldBe 200
                page.body() shouldContain "이 창을 닫고 앱으로 돌아가세요."
                page.body() shouldNotContain code
                credential.code shouldBe code
                credential.clientId shouldBe clientId
                credential.redirectUri shouldBe receiver.redirectUri
                s256CodeChallenge(credential.codeVerifier.orEmpty()) shouldBe codeChallenge
            }
        }

        test("TC-LOGIN-DATA-017 응답을 받아 기다리기를 마친 로컬 수신 주소는 더 이상 요청을 받지 않는다") {
            val receiver = LoopbackRedirectReceiver()
            val redirectUri = receiver.redirectUri

            receiver.use {
                get("$redirectUri?code=${fixtureMonkey.nonBlankString().urlEncode()}")
                it.waitForParameters()
            }

            shouldThrow<ConnectException> { get(redirectUri) }
        }

        test("TC-LOGIN-DATA-018 오류 응답을 그대로 받고 결과 페이지에는 오류 내용 없이 기본 문구를 표시한다") {
            val error = "error-${fixtureMonkey.nonBlankString()}"
            val state = "state-${fixtureMonkey.nonBlankString()}"

            LoopbackRedirectReceiver(locale = Locale.ENGLISH).use { receiver ->
                val page = get("${receiver.redirectUri}?error=${error.urlEncode()}&state=${state.urlEncode()}")
                val parameters = receiver.waitForParameters()

                page.body() shouldContain "You can close this window and return to the app."
                page.body() shouldNotContain error
                parameters shouldBe mapOf("error" to error, "state" to state)
            }
        }
    })

private fun get(url: String): HttpResponse<String> =
    HttpClient
        .newHttpClient()
        .send(HttpRequest.newBuilder(URI(url)).GET().build(), HttpResponse.BodyHandlers.ofString())

private fun String.urlEncode(): String = URLEncoder.encode(this, Charsets.UTF_8)
