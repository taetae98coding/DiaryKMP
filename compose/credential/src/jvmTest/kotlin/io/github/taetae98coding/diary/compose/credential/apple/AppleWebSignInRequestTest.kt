package io.github.taetae98coding.diary.compose.credential.apple

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.authentication.AppleAuthorization
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class AppleWebSignInRequestTest :
    FunSpec({
        test("TC-0b76b517-494f-45d4-9d15-e0d090eda980 상태 값이 다르거나 없는 응답은 인증 결과로 받지 않는다") {
            val request = appleWebSignInRequest()
            val idToken = "id-token-${fixtureMonkey.giveMeOne<String>()}"
            val stateList = listOf("other-${request.state}", null)

            stateList.forEach { state ->
                shouldThrowExactly<AppleCredentialsException> {
                    request.toResult(AppleWebSignInResponse(idToken = idToken, code = null, state = state, error = null)).credential
                }
            }
        }

        test("TC-974c8aaa-a635-4c47-9a84-c8eb3bcf973d idToken이 있으면 이번 시도의 nonce와 함께 인증 결과가 된다") {
            val nonce = "nonce-${fixtureMonkey.giveMeOne<String>()}"
            val request = appleWebSignInRequest(nonce = nonce)
            val idToken = "id-token-${fixtureMonkey.giveMeOne<String>()}"

            val credential = request.toResult(AppleWebSignInResponse(idToken = idToken, code = null, state = request.state, error = null)).credential

            credential.idToken shouldBe idToken
            credential.nonce shouldBe nonce
        }

        test("TC-974c8aaa-a635-4c47-9a84-c8eb3bcf973d Apple의 사용자 취소 오류는 사용자 취소로 처리한다") {
            val request = appleWebSignInRequest()

            shouldThrowExactly<AppleCredentialsUserCancelException> {
                request.toResult(AppleWebSignInResponse(idToken = null, code = null, state = request.state, error = "user_cancelled_authorize")).credential
            }
        }

        test("TC-974c8aaa-a635-4c47-9a84-c8eb3bcf973d 그 외 오류와 빈 응답은 인증 결과를 받지 못한 것으로 처리한다") {
            val request = appleWebSignInRequest()
            val errorList = listOf("invalid_request", null)

            errorList.forEach { error ->
                shouldThrowExactly<AppleCredentialsException> {
                    request.toResult(AppleWebSignInResponse(idToken = null, code = null, state = request.state, error = error)).credential
                }
            }
        }

        test("TC-14907d46-2174-40dd-a015-c832d521d41c 이번 시도의 응답에 인가 코드가 있으면 그 코드와 이 앱의 식별 값을 받는다") {
            val clientId = "client-${fixtureMonkey.giveMeOne<String>()}"
            val request = appleWebSignInRequest(clientId = clientId)
            val code = "code-${fixtureMonkey.giveMeOne<String>()}"

            val result = request.toResult(AppleWebSignInResponse(idToken = "id-token-${fixtureMonkey.giveMeOne<String>()}", code = code, state = request.state, error = null))

            result.authorization shouldBe AppleAuthorization(code = code, clientId = clientId)
        }

        test("TC-14907d46-2174-40dd-a015-c832d521d41c 인가 코드가 없거나 상태 값이 다르면 인가 코드를 받지 못한다") {
            val request = appleWebSignInRequest()

            request.toResult(AppleWebSignInResponse(idToken = "id-token-${fixtureMonkey.giveMeOne<String>()}", code = null, state = request.state, error = null)).authorization shouldBe null
            shouldThrowExactly<AppleCredentialsException> {
                request.toResult(AppleWebSignInResponse(idToken = "id-token-${fixtureMonkey.giveMeOne<String>()}", code = "code-${fixtureMonkey.giveMeOne<String>()}", state = "other-${request.state}", error = null))
            }
            shouldThrowExactly<AppleCredentialsUserCancelException> {
                request.toResult(AppleWebSignInResponse(idToken = null, code = null, state = request.state, error = "user_cancelled_authorize"))
            }
        }

        test("응답 매개변수에서 인가 코드를 읽는다") {
            val code = "code-${fixtureMonkey.giveMeOne<String>()}"
            val parameters =
                mapOf(
                    "id_token" to "id-token-${fixtureMonkey.giveMeOne<String>()}",
                    "code" to code,
                    "state" to "state-${fixtureMonkey.giveMeOne<String>()}",
                )

            AppleWebSignInResponse.fromParameters(parameters::get).code shouldBe code
        }
    })

private fun appleWebSignInRequest(
    nonce: String = "nonce-${fixtureMonkey.giveMeOne<String>()}",
    clientId: String = "client-${fixtureMonkey.giveMeOne<String>()}",
): AppleWebSignInRequest =
    AppleWebSignInRequest(
        authorizationUrl = "https://appleid.apple.com/auth/authorize?state=${fixtureMonkey.giveMeOne<String>()}",
        state =
            AppleWebSignInState.encode(
                token = fixtureMonkey.giveMeOne<String>(),
                returnUri = "http://127.0.0.1:${fixtureMonkey.giveMeOne<Int>().toUInt()}/callback",
            ),
        nonce = nonce,
        clientId = clientId,
    )
