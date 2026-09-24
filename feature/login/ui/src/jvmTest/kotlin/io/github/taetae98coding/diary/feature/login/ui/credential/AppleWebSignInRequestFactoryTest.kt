package io.github.taetae98coding.diary.feature.login.ui.credential

import com.google.api.client.http.GenericUrl
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class AppleWebSignInRequestFactoryTest :
    FunSpec({
        test("TC-LOGIN-DATA-010 로그인 요청은 서버 콜백 주소, 이번 시도의 nonce, 복귀 주소를 담는다") {
            val config = appleCredentialsConfig()
            val returnUri = "http://127.0.0.1:${fixtureMonkey.giveMeOne<Int>().toUInt()}/callback"
            val idToken = "id-token-${fixtureMonkey.giveMeOne<String>()}"

            val request = AppleWebSignInRequestFactory(config = config).create(returnUri = returnUri)
            val authorizationRequest = GenericUrl(request.authorizationUrl)
            val credential = request.toCredential(AppleWebSignInResponse(idToken = idToken, state = request.state, error = null))

            request.authorizationUrl shouldStartWith "https://appleid.apple.com/auth/authorize?"
            authorizationRequest.getFirst("client_id") shouldBe config.clientId
            authorizationRequest.getFirst("redirect_uri") shouldBe config.callbackUrl
            authorizationRequest.getFirst("response_type") shouldBe "code id_token"
            authorizationRequest.getFirst("response_mode") shouldBe "form_post"
            authorizationRequest.getFirst("scope") shouldBe "email"
            authorizationRequest.getFirst("state") shouldBe request.state
            authorizationRequest.getFirst("nonce") shouldBe CredentialsNonce.hash(credential.nonce)
            AppleWebSignInState.decodeReturnUri(request.state) shouldBe returnUri
        }

        test("TC-LOGIN-DATA-011 새 로그인 시도는 새로운 nonce와 상태 값을 사용한다") {
            val factory = AppleWebSignInRequestFactory(config = appleCredentialsConfig())
            val returnUri = "io.github.taetae98coding.diary://apple-sign-in-${fixtureMonkey.giveMeOne<String>()}"

            val first = factory.create(returnUri = returnUri)
            val second = factory.create(returnUri = returnUri)
            val idToken = "id-token-${fixtureMonkey.giveMeOne<String>()}"
            val firstNonce = first.toCredential(AppleWebSignInResponse(idToken = idToken, state = first.state, error = null)).nonce
            val secondNonce = second.toCredential(AppleWebSignInResponse(idToken = idToken, state = second.state, error = null)).nonce

            first.state shouldNotBe second.state
            firstNonce shouldNotBe secondNonce
            GenericUrl(first.authorizationUrl).getFirst("nonce") shouldNotBe GenericUrl(second.authorizationUrl).getFirst("nonce")
        }

        test("상태 값에서 복귀 주소를 되찾지 못하면 null을 돌려준다") {
            AppleWebSignInState.decodeReturnUri("token-without-return-uri") shouldBe null
            AppleWebSignInState.decodeReturnUri("token.%%%") shouldBe null
        }
    })

private fun appleCredentialsConfig(): AppleCredentialsConfig =
    AppleCredentialsConfig(
        clientId = "io.github.taetae98coding.diary.web-${fixtureMonkey.giveMeOne<String>()}",
        callbackUrl = "https://${fixtureMonkey.giveMeOne<Long>().toULong()}.supabase.co/functions/v1/v1-session-apple-callback",
    )
