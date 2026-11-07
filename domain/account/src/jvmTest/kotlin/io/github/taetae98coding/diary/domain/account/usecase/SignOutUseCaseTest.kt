package io.github.taetae98coding.diary.domain.account.usecase

import io.github.taetae98coding.diary.domain.account.repository.LoginProviderRepository
import io.github.taetae98coding.diary.domain.account.repository.SessionRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeSuccess
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.just
import io.mockk.mockk

class SignOutUseCaseTest :
    BehaviorSpec({
        Given("사용자가 Diary 계정으로 로그인해 있다") {
            val sessionRepository = mockk<SessionRepository>()
            coEvery { sessionRepository.delete() } just Runs
            val loginProviderRepository = mockk<LoginProviderRepository>()
            coEvery { loginProviderRepository.unset() } just Runs
            val useCase = SignOutUseCase(sessionRepository = sessionRepository, loginProviderRepository = loginProviderRepository)

            When("Diary에서 로그아웃한다") {
                val result = useCase(parameter = Unit)

                // 로그아웃은 Diary 세션만 다루므로 앱 안 웹 표시 수단의 로그인 정보와 보관한 세션 쿠키에 닿는 경로가 없다.
                Then("TC-b99d369e-7d38-4b6c-bb80-99d14f627f6f Diary 세션만 지우고 웹 로그인 정보는 지우지 않는다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 1) { sessionRepository.delete() }
                    confirmVerified(sessionRepository)
                }

                Then("TC-a9e03db2-b461-4351-8506-d90fa5fe6a90 기기에 저장된 로그인 정보를 지우는 요청이 한 번 이루어진다") {
                    coVerify(exactly = 1) { sessionRepository.delete() }
                }

                Then("TC-649f316c-b797-4ee8-bcde-3ef39857c274 로그인 수단 기록을 지운다") {
                    coVerify(exactly = 1) { loginProviderRepository.unset() }
                }
            }
        }
    })
