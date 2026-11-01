@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.login.ui.home

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential
import io.github.taetae98coding.diary.feature.login.ui.credential.GoogleCredentialsException
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class LoginPlatformSignInStateTest : FunSpec() {
    init {
        test("끝을 알 수 있는 흐름은 끝날 때까지 진행 중이고 그동안 새 흐름을 받지 않는다") {
            runTest {
                val credential = fixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
                val state = LoginPlatformSignInState()
                val platformResult = CompletableDeferred<GoogleCredential>()
                var secondStarted = false

                val first = async { state.signIn(isEndDetectable = true) { platformResult.await() } }
                runCurrent()
                state.isInProgress.shouldBeTrue()
                val second = state.signIn(isEndDetectable = true) { secondStarted = true }
                platformResult.complete(credential)

                second.shouldBeNull()
                secondStarted.shouldBeFalse()
                first.await() shouldBe credential
                state.isInProgress.shouldBeFalse()
            }
        }

        test("끝을 알 수 있는 흐름이 실패하면 진행 중이 풀리고 실패를 그대로 던진다") {
            runTest {
                val state = LoginPlatformSignInState()

                shouldThrow<GoogleCredentialsException> {
                    state.signIn<GoogleCredential>(isEndDetectable = true) { throw GoogleCredentialsException() }
                }
                state.isInProgress.shouldBeFalse()
            }
        }

        test("끝을 알 수 없는 흐름은 진행 중으로 표시하지 않고 새 흐름을 열면 진행 중이던 흐름을 취소한다") {
            runTest {
                val state = LoginPlatformSignInState()
                val platformResultList = List(size = 2) { CompletableDeferred<GoogleCredential>() }

                val first = async { state.signIn(isEndDetectable = false) { platformResultList[0].await() } }
                runCurrent()
                state.isInProgress.shouldBeFalse()
                val second = async { state.signIn(isEndDetectable = false) { platformResultList[1].await() } }
                runCurrent()

                first.isCancelled.shouldBeTrue()
                second.isActive.shouldBeTrue()
                second.cancel()
            }
        }

        test("결과를 받은 흐름은 새 흐름을 열어도 취소되지 않는다") {
            runTest {
                val credential = fixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
                val state = LoginPlatformSignInState()
                val afterSignIn = CompletableDeferred<Unit>()

                val first =
                    async {
                        state.signIn(isEndDetectable = false) { credential }
                        afterSignIn.await()
                    }
                runCurrent()
                val second = async { state.signIn(isEndDetectable = false) { CompletableDeferred<GoogleCredential>().await() } }
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
                val state = LoginPlatformSignInState()

                state.signIn(isEndDetectable = false) { firstCredential } shouldBe firstCredential
                state.signIn(isEndDetectable = false) { nextCredential } shouldBe nextCredential
            }
        }
    }
}
