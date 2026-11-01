package io.github.taetae98coding.diary.domain.account.usecase

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
            val useCase = SignOutUseCase(sessionRepository = sessionRepository)

            When("Diary에서 로그아웃한다") {
                val result = useCase(parameter = Unit)

                // 로그아웃은 Diary 세션만 다루므로 앱 안 웹 표시 수단의 로그인 정보와 보관한 세션 쿠키에 닿는 경로가 없다.
                Then("TC-b99d369e-7d38-4b6c-bb80-99d14f627f6f Diary 세션만 지우고 웹 로그인 정보는 지우지 않는다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 1) { sessionRepository.delete() }
                    confirmVerified(sessionRepository)
                }
            }
        }
    })
