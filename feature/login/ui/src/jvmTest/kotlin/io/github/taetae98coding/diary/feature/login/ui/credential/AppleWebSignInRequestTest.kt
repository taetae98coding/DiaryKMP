package io.github.taetae98coding.diary.feature.login.ui.credential

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class AppleWebSignInRequestTest :
    FunSpec({
        test("TC-LOGIN-DOMAIN-005 상태 값이 다르거나 없는 응답은 인증 결과로 받지 않는다") {
            val request = appleWebSignInRequest()
            val idToken = "id-token-${fixtureMonkey.giveMeOne<String>()}"
            val stateList = listOf("other-${request.state}", null)

            stateList.forEach { state ->
                shouldThrowExactly<AppleCredentialsException> {
                    request.toCredential(AppleWebSignInResponse(idToken = idToken, state = state, error = null))
                }
            }
        }

        test("TC-LOGIN-DOMAIN-006 idToken이 있으면 이번 시도의 nonce와 함께 인증 결과가 된다") {
            val nonce = "nonce-${fixtureMonkey.giveMeOne<String>()}"
            val request = appleWebSignInRequest(nonce = nonce)
            val idToken = "id-token-${fixtureMonkey.giveMeOne<String>()}"

            val credential = request.toCredential(AppleWebSignInResponse(idToken = idToken, state = request.state, error = null))

            credential.idToken shouldBe idToken
            credential.nonce shouldBe nonce
        }

        test("TC-LOGIN-DOMAIN-006 Apple의 사용자 취소 오류는 사용자 취소로 처리한다") {
            val request = appleWebSignInRequest()

            shouldThrowExactly<AppleCredentialsUserCancelException> {
                request.toCredential(AppleWebSignInResponse(idToken = null, state = request.state, error = "user_cancelled_authorize"))
            }
        }

        test("TC-LOGIN-DOMAIN-006 그 외 오류와 빈 응답은 인증 결과를 받지 못한 것으로 처리한다") {
            val request = appleWebSignInRequest()
            val errorList = listOf("invalid_request", null)

            errorList.forEach { error ->
                shouldThrowExactly<AppleCredentialsException> {
                    request.toCredential(AppleWebSignInResponse(idToken = null, state = request.state, error = error))
                }
            }
        }
    })

private fun appleWebSignInRequest(nonce: String = "nonce-${fixtureMonkey.giveMeOne<String>()}"): AppleWebSignInRequest =
    AppleWebSignInRequest(
        authorizationUrl = "https://appleid.apple.com/auth/authorize?state=${fixtureMonkey.giveMeOne<String>()}",
        state =
            AppleWebSignInState.encode(
                token = fixtureMonkey.giveMeOne<String>(),
                returnUri = "http://127.0.0.1:${fixtureMonkey.giveMeOne<Int>().toUInt()}/callback",
            ),
        nonce = nonce,
    )
