package io.github.taetae98coding.diary.feature.login.ui.credential

import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.library.fixturemonkey.nonBlankString
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlin.coroutines.cancellation.CancellationException

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class GoogleAuthorizationCodeTest :
    FunSpec({
        test("TC-LOGIN-DOMAIN-007 인가 코드가 있으면 인증 결과의 인가 코드가 된다") {
            val code = fixtureMonkey.nonBlankString()

            googleAuthorizationCodeOrThrow(code = code, error = "") shouldBe code
        }

        test("TC-LOGIN-DOMAIN-007 사용자가 동의를 거부했다는 오류는 사용자 취소로 처리한다") {
            shouldThrowExactly<GoogleCredentialsUserCancelException> {
                googleAuthorizationCodeOrThrow(code = "", error = "access_denied")
            }
        }

        test("TC-LOGIN-DOMAIN-007 그 밖의 오류와 빈 응답은 인증 결과를 받지 못한 실패로 처리한다") {
            val errorList = listOf("invalid_scope", "server_error", "")

            errorList.forEach { error ->
                shouldThrowExactly<GoogleCredentialsException> {
                    googleAuthorizationCodeOrThrow(code = "", error = error)
                }
            }
        }

        test("취소와 이미 분류된 실패는 그대로 두고, 나머지 예외만 인증 결과 획득 실패로 감싼다") {
            val cancellation = CancellationException()
            val userCancel = GoogleCredentialsUserCancelException()
            val platformFailure = UnsupportedOperationException()

            cancellation.toGoogleCredentialsFailure() shouldBeSameInstanceAs cancellation
            userCancel.toGoogleCredentialsFailure() shouldBeSameInstanceAs userCancel
            platformFailure.toGoogleCredentialsFailure().shouldBeInstanceOf<GoogleCredentialsException>().cause shouldBeSameInstanceAs platformFailure
        }
    })
