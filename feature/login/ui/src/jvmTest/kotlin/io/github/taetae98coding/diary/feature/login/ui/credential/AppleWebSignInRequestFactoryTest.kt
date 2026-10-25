package io.github.taetae98coding.diary.feature.login.ui.credential

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
        test("TC-d0a6a2b3-9ae1-4ea7-9318-976bb99e0020 로그인 요청은 서버 콜백 주소, 이번 시도의 nonce, 복귀 주소를 담는다") {
            val config = fixtureMonkey.appleCredentialsConfig()
            val returnUri = "http://127.0.0.1:${fixtureMonkey.giveMeOne<Int>().toUInt()}/callback"
            val idToken = "id-token-${fixtureMonkey.giveMeOne<String>()}"

            val request = AppleWebSignInRequestFactory(config = config).create(returnUri = returnUri)
            val credential = request.toCredential(AppleWebSignInResponse(idToken = idToken, state = request.state, error = null))

            request.authorizationUrl shouldStartWith "https://appleid.apple.com/auth/authorize?"
            request.authorizationUrl.queryParameter("client_id") shouldBe config.clientId
            request.authorizationUrl.queryParameter("redirect_uri") shouldBe config.callbackUrl
            request.authorizationUrl.queryParameter("response_type") shouldBe "code id_token"
            request.authorizationUrl.queryParameter("response_mode") shouldBe "form_post"
            request.authorizationUrl.queryParameter("scope") shouldBe "email"
            request.authorizationUrl.queryParameter("state") shouldBe request.state
            request.authorizationUrl.queryParameter("nonce") shouldBe CredentialsNonce.hash(credential.nonce)
            AppleWebSignInState.decodeReturnUri(request.state) shouldBe returnUri
        }

        test("TC-8863804f-e996-4934-9845-5dbb496a9704 새 로그인 시도는 새로운 nonce와 상태 값을 사용한다") {
            val factory = AppleWebSignInRequestFactory(config = fixtureMonkey.appleCredentialsConfig())
            val returnUri = "io.github.taetae98coding.diary://apple-sign-in-${fixtureMonkey.giveMeOne<String>()}"

            val first = factory.create(returnUri = returnUri)
            val second = factory.create(returnUri = returnUri)
            val idToken = "id-token-${fixtureMonkey.giveMeOne<String>()}"
            val firstNonce = first.toCredential(AppleWebSignInResponse(idToken = idToken, state = first.state, error = null)).nonce
            val secondNonce = second.toCredential(AppleWebSignInResponse(idToken = idToken, state = second.state, error = null)).nonce

            first.state shouldNotBe second.state
            firstNonce shouldNotBe secondNonce
            first.authorizationUrl.queryParameter("nonce") shouldNotBe second.authorizationUrl.queryParameter("nonce")
        }

        test("상태 값에서 복귀 주소를 되찾지 못하면 null을 돌려준다") {
            AppleWebSignInState.decodeReturnUri("token-without-return-uri") shouldBe null
            AppleWebSignInState.decodeReturnUri("token.%%%") shouldBe null
        }
    })
