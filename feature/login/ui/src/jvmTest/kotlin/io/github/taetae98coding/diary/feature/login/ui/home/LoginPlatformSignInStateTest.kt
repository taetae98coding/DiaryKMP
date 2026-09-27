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

        test("끝을 알 수 없는 흐름은 진행 중으로 표시하지 않고 여러 흐름을 함께 받는다") {
            runTest {
                val state = LoginPlatformSignInState()
                val platformResultList = List(size = 2) { CompletableDeferred<GoogleCredential>() }

                val flowList = platformResultList.map { result -> async { state.signIn(isEndDetectable = false) { result.await() } } }
                runCurrent()

                state.isInProgress.shouldBeFalse()
                flowList.forEach { flow -> flow.isActive.shouldBeTrue() }
                flowList.forEach { flow -> flow.cancel() }
            }
        }

        test("먼저 받아들인 결과 뒤에 끝난 이전 흐름의 결과와 실패는 null로 버린다") {
            runTest {
                val firstCredential = fixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
                val lateCredential = fixtureMonkey.giveMeOne<GoogleCredential.IdToken>()
                val state = LoginPlatformSignInState()
                val platformResultList = List(size = 3) { CompletableDeferred<GoogleCredential>() }

                val flowList = platformResultList.map { result -> async { state.signIn(isEndDetectable = false) { result.await() } } }
                runCurrent()
                platformResultList[0].complete(firstCredential)
                platformResultList[1].complete(lateCredential)
                platformResultList[2].completeExceptionally(GoogleCredentialsException())

                flowList[0].await() shouldBe firstCredential
                flowList[1].await().shouldBeNull()
                flowList[2].await().shouldBeNull()
            }
        }

        test("결과를 받아들인 뒤 새로 시작한 흐름의 결과는 받는다") {
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
