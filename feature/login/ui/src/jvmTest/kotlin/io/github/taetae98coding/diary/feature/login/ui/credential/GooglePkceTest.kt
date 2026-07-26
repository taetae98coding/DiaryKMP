package io.github.taetae98coding.diary.feature.login.ui.credential

import com.google.api.client.http.GenericUrl
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.security.MessageDigest
import java.util.Base64

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class GooglePkceTest :
    FunSpec({
        test("TC-LOGIN-DATA-008 같은 로그인 시도의 S256 challenge와 verifier를 사용한다") {
            val randomBytes = randomBytes()
            val clientId = googleClientId()
            val redirectUri = loopbackRedirectUri()
            val authorizationCode = fixtureMonkey.giveMeOne<String>()
            val pkceFactory = GooglePkceFactory { target -> randomBytes.copyInto(target) }
            val attemptFactory =
                GoogleAuthorizationCodeAttemptFactory(
                    clientId = clientId,
                    googlePkceFactory = pkceFactory,
                )

            val attempt = attemptFactory.create(redirectUri = redirectUri)
            val authorizationRequest = GenericUrl(attempt.authorizationUrl)
            val credential = attempt.credential(code = authorizationCode)

            authorizationRequest.getFirst("code_challenge_method") shouldBe "S256"
            authorizationRequest.getFirst("code_challenge") shouldBe codeChallenge(credential.codeVerifier.orEmpty())
            credential.code shouldBe authorizationCode
            credential.clientId shouldBe clientId
            credential.redirectUri shouldBe redirectUri
        }

        test("TC-LOGIN-DATA-009 새 로그인 시도는 새로운 PKCE 값을 사용한다") {
            val firstRandomBytes = randomBytes()
            val secondRandomBytes = firstRandomBytes.map { byte -> byte.toInt().inv().toByte() }.toByteArray()
            var attemptCount = 0
            val pkceFactory =
                GooglePkceFactory { target ->
                    if (attemptCount++ == 0) {
                        firstRandomBytes
                    } else {
                        secondRandomBytes
                    }.copyInto(target)
                }
            val attemptFactory =
                GoogleAuthorizationCodeAttemptFactory(
                    clientId = googleClientId(),
                    googlePkceFactory = pkceFactory,
                )

            val firstAttempt = attemptFactory.create(redirectUri = loopbackRedirectUri())
            val secondAttempt = attemptFactory.create(redirectUri = loopbackRedirectUri())
            val firstCredential = firstAttempt.credential(code = fixtureMonkey.giveMeOne<String>())
            val secondCredential = secondAttempt.credential(code = fixtureMonkey.giveMeOne<String>())

            firstCredential.codeVerifier shouldNotBe secondCredential.codeVerifier
            GenericUrl(firstAttempt.authorizationUrl).getFirst("code_challenge") shouldNotBe
                GenericUrl(secondAttempt.authorizationUrl).getFirst("code_challenge")
        }

        test("RFC 7636 S256 공식 벡터로 challenge를 만든다") {
            val randomBytes = Base64.getUrlDecoder().decode(RFC_7636_CODE_VERIFIER)
            val pkceFactory = GooglePkceFactory { target -> randomBytes.copyInto(target) }

            val actual = pkceFactory.create()

            actual.codeVerifier shouldBe RFC_7636_CODE_VERIFIER
            actual.codeChallenge shouldBe RFC_7636_CODE_CHALLENGE
        }

        test("verifier는 256비트 엔트로피의 Base64URL 무패딩 문자열이다") {
            val actual = GooglePkceFactory().create().codeVerifier

            actual.length shouldBe 43
            PKCE_CODE_VERIFIER_REGEX.matches(actual) shouldBe true
        }
    })

private fun randomBytes(): ByteArray = ByteArray(32) { fixtureMonkey.giveMeOne<Int>().toByte() }

private fun googleClientId(): String = "${fixtureMonkey.giveMeOne<Long>().toULong()}.apps.googleusercontent.com"

private fun loopbackRedirectUri(): String {
    val port =
        1_024 +
            fixtureMonkey
                .giveMeOne<Int>()
                .toUInt()
                .rem(64_512u)
                .toInt()
    return "http://127.0.0.1:$port/Callback"
}

private fun codeChallenge(codeVerifier: String): String =
    Base64
        .getUrlEncoder()
        .withoutPadding()
        .encodeToString(
            MessageDigest
                .getInstance("SHA-256")
                .digest(codeVerifier.encodeToByteArray()),
        )

private val PKCE_CODE_VERIFIER_REGEX = Regex("^[A-Za-z0-9._~-]{43,128}$")
private const val RFC_7636_CODE_VERIFIER = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"
private const val RFC_7636_CODE_CHALLENGE = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM"
