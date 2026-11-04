@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.compose.credential

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.credential.google.GoogleCredentialsException
import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class PlatformSignInStateTest : FunSpec() {
    init {
        test("흐름이 실패하면 실패를 그대로 던진다") {
            runTest {
                val state = PlatformSignInState()

                shouldThrow<GoogleCredentialsException> {
                    state.signIn<GoogleCredential> { throw GoogleCredentialsException() }
                }
            }
        }

        test("새 흐름을 열면 진행 중이던 흐름을 취소한다") {
            runTest {
                val state = PlatformSignInState()
                val platformResultList = List(size = 2) { CompletableDeferred<GoogleCredential>() }

                val first = async { state.signIn { platformResultList[0].await() } }
                runCurrent()
                val second = async { state.signIn { platformResultList[1].await() } }
                runCurrent()

                first.isCancelled.shouldBeTrue()
                second.isActive.shouldBeTrue()
                second.cancel()
            }
        }

        test("결과를 받은 흐름은 새 흐름을 열어도 취소되지 않는다") {
            runTest {
                val credential = fixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
                val state = PlatformSignInState()
                val afterSignIn = CompletableDeferred<Unit>()

                val first =
                    async {
                        state.signIn { credential }
                        afterSignIn.await()
                    }
                runCurrent()
                val second = async { state.signIn { CompletableDeferred<GoogleCredential>().await() } }
                runCurrent()

                first.isActive.shouldBeTrue()
                afterSignIn.complete(Unit)
                first.await()
                second.cancel()
            }
        }

        test("결과를 받은 뒤 새로 시작한 흐름의 결과는 받는다") {
            runTest {
                val firstCredential = fixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
                val nextCredential = fixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
                val state = PlatformSignInState()

                state.signIn { firstCredential } shouldBe firstCredential
                state.signIn { nextCredential } shouldBe nextCredential
            }
        }
    }
}
