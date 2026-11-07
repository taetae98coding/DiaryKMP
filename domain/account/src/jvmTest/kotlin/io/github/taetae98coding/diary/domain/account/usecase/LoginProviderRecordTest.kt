package io.github.taetae98coding.diary.domain.account.usecase

import io.github.taetae98coding.diary.core.model.authentication.AppleCredential
import io.github.taetae98coding.diary.core.model.authentication.GoogleCredential
import io.github.taetae98coding.diary.core.model.authentication.LoginProvider
import io.github.taetae98coding.diary.core.testing.authentication.appleCredential
import io.github.taetae98coding.diary.core.testing.authentication.googleCredentialIdToken
import io.github.taetae98coding.diary.domain.account.repository.LoginProviderRepository
import io.github.taetae98coding.diary.domain.account.repository.SessionRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk

class LoginProviderRecordTest :
    BehaviorSpec({
        Given("Google 로그인과 Apple 로그인이 모두 성공하도록 준비되어 있다") {
            val sessionRepository = mockk<SessionRepository>()
            coEvery { sessionRepository.create(any<GoogleCredential>()) } just Runs
            coEvery { sessionRepository.create(any<AppleCredential>()) } just Runs
            val loginProviderRepository = mockk<LoginProviderRepository>()
            coEvery { loginProviderRepository.set(provider = any()) } just Runs

            When("Google 로그인을 완료한다") {
                SignInWithGoogleUseCase(sessionRepository = sessionRepository, loginProviderRepository = loginProviderRepository)(
                    parameter = Arb.googleCredentialIdToken().next(),
                )

                Then("TC-649f316c-b797-4ee8-bcde-3ef39857c274 Google 로그인을 기록한다") {
                    coVerify(exactly = 1) { loginProviderRepository.set(provider = LoginProvider.GOOGLE) }
                }
            }

            When("Apple 로그인을 완료한다") {
                SignInWithAppleUseCase(sessionRepository = sessionRepository, loginProviderRepository = loginProviderRepository)(
                    parameter = Arb.appleCredential().next(),
                )

                Then("TC-649f316c-b797-4ee8-bcde-3ef39857c274 Apple 로그인을 기록한다") {
                    coVerify(exactly = 1) { loginProviderRepository.set(provider = LoginProvider.APPLE) }
                }
            }
        }

        Given("오류 보고 기록 수단이 등록되어 있고 앱 세션 설정은 성공하지만 로그인 수단 기록은 실패하도록 준비되어 있다") {
            val sessionRepository = mockk<SessionRepository>()
            coEvery { sessionRepository.create(any<GoogleCredential>()) } just Runs
            coEvery { sessionRepository.create(any<AppleCredential>()) } just Runs
            val loginProviderRepository = mockk<LoginProviderRepository>()
            coEvery { loginProviderRepository.set(provider = any()) } throws IllegalStateException("record failed")
            val reportList = recordCrashlyticsLog()

            When("Google 로그인을 완료한다") {
                val result =
                    SignInWithGoogleUseCase(sessionRepository = sessionRepository, loginProviderRepository = loginProviderRepository)(
                        parameter = Arb.googleCredentialIdToken().next(),
                    )

                Then("TC-0487b248-3a4f-4251-98cc-a65e4e0679bf Google 로그인은 성공하고 오류 보고가 남지 않는다") {
                    result.shouldBeSuccess()
                    reportList.shouldBeEmpty()
                }
            }

            When("Apple 로그인을 완료한다") {
                val result =
                    SignInWithAppleUseCase(sessionRepository = sessionRepository, loginProviderRepository = loginProviderRepository)(
                        parameter = Arb.appleCredential().next(),
                    )

                Then("TC-0487b248-3a4f-4251-98cc-a65e4e0679bf Apple 로그인은 성공하고 오류 보고가 남지 않는다") {
                    result.shouldBeSuccess()
                    reportList.shouldBeEmpty()
                }
            }
        }

        Given("Apple 로그인의 앱 세션 설정이 실패하도록 준비되어 있다") {
            val sessionRepository = mockk<SessionRepository>()
            coEvery { sessionRepository.create(any<AppleCredential>()) } throws IllegalStateException("session failed")
            val loginProviderRepository = mockk<LoginProviderRepository>()
            coEvery { loginProviderRepository.set(provider = any()) } just Runs

            When("Apple 로그인을 시도한다") {
                SignInWithAppleUseCase(sessionRepository = sessionRepository, loginProviderRepository = loginProviderRepository)(
                    parameter = Arb.appleCredential().next(),
                )

                Then("TC-afe16b84-85ac-4d0d-89a1-4b11cfeed28a 로그인 수단을 기록하지 않는다") {
                    coVerify(exactly = 0) { loginProviderRepository.set(provider = any()) }
                }
            }
        }
    })
