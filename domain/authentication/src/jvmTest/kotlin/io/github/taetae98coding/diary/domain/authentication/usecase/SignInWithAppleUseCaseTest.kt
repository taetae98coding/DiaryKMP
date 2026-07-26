package io.github.taetae98coding.diary.domain.authentication.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.authentication.AppleCredential
import io.github.taetae98coding.diary.domain.authentication.repository.SessionRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class SignInWithAppleUseCaseTest :
    BehaviorSpec({
        Given("Apple credential과 성공하는 repository가 준비되어 있다") {
            val credential = fixtureMonkey.giveMeOne<AppleCredential>()
            val repository = mockk<SessionRepository>()
            coEvery { repository.create(credential) } returns Unit
            val useCase = SignInWithAppleUseCase(sessionRepository = repository)

            When("해당 credential로 로그인을 요청한다") {
                val result = useCase(parameter = credential)

                Then("TC-LOGIN-DOMAIN-002 성공 Result를 반환한다") {
                    result.shouldBeSuccess()
                }

                Then("TC-LOGIN-DOMAIN-002 idToken과 nonce를 담은 credential을 한 번 전달한다") {
                    coVerify(exactly = 1) {
                        repository.create(credential)
                    }
                }
            }
        }

        Given("Apple credential과 실패하는 repository가 준비되어 있다") {
            val credential = fixtureMonkey.giveMeOne<AppleCredential>()
            val repository = mockk<SessionRepository>()
            coEvery { repository.create(credential) } throws AppleUseCaseTestException()
            val useCase = SignInWithAppleUseCase(sessionRepository = repository)

            When("해당 credential로 로그인을 요청한다") {
                val result = useCase(parameter = credential)

                Then("실패 Result를 반환한다") {
                    result.shouldBeFailure<AppleUseCaseTestException>()
                }
            }
        }

        Given("오류 보고 기록 수단이 등록되어 있고 Apple 앱 로그인이 특정 원인으로 실패하도록 준비되어 있다") {
            val credential = fixtureMonkey.giveMeOne<AppleCredential>()
            val failure = AppleUseCaseTestException()
            val repository = mockk<SessionRepository>()
            coEvery { repository.create(credential) } throws failure
            val useCase = SignInWithAppleUseCase(sessionRepository = repository)

            val reportList = recordCrashlyticsLog()

            When("해당 credential로 로그인을 요청한다") {
                useCase(parameter = credential)

                Then("TC-LOGIN-DOMAIN-003 그 원인을 담은 오류 보고가 한 번 남는다") {
                    val report = reportList.single()

                    report.throwable shouldBeSameInstanceAs failure
                    report.message shouldContain "SignInWithAppleUseCase"
                }
            }
        }

        Given("오류 보고 기록 수단이 등록되어 있고 Apple 앱 로그인이 성공하도록 준비되어 있다") {
            val credential = fixtureMonkey.giveMeOne<AppleCredential>()
            val repository = mockk<SessionRepository>()
            coEvery { repository.create(credential) } returns Unit
            val useCase = SignInWithAppleUseCase(sessionRepository = repository)

            val reportList = recordCrashlyticsLog()

            When("해당 credential로 로그인을 요청한다") {
                useCase(parameter = credential)

                Then("TC-LOGIN-DOMAIN-004 오류 보고가 남지 않는다") {
                    reportList.shouldBeEmpty()
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}

private class AppleUseCaseTestException : RuntimeException("sign in failure")
