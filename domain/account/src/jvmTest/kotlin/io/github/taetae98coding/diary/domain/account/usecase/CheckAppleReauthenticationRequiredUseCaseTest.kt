package io.github.taetae98coding.diary.domain.account.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.UserData
import io.github.taetae98coding.diary.core.model.authentication.LoginProvider
import io.github.taetae98coding.diary.domain.account.repository.LoginProviderRepository
import io.github.taetae98coding.diary.domain.account.repository.UserDataRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class CheckAppleReauthenticationRequiredUseCaseTest :
    BehaviorSpec({
        listOf(
            Triple(LoginProvider.APPLE, setOf(LoginProvider.APPLE), true),
            Triple(LoginProvider.GOOGLE, setOf(LoginProvider.GOOGLE), false),
            Triple(LoginProvider.GOOGLE, setOf(LoginProvider.GOOGLE, LoginProvider.APPLE), false),
            Triple(null, setOf(LoginProvider.APPLE), true),
            Triple(null, setOf(LoginProvider.GOOGLE, LoginProvider.APPLE), true),
            Triple(null, setOf(LoginProvider.GOOGLE), false),
        ).forEach { (record, linkedSet, expected) ->
            Given("로그인 수단 기록이 $record 이고 계정에 $linkedSet 이 연결되어 있다") {
                val loginProviderRepository = mockk<LoginProviderRepository>()
                coEvery { loginProviderRepository.read() } returns record
                val userDataRepository = mockk<UserDataRepository>()
                every { userDataRepository.get() } returns flowOf(fixtureMonkey.giveMeOne<UserData>().copy(loginProviderSet = linkedSet))
                val useCase =
                    CheckAppleReauthenticationRequiredUseCase(
                        loginProviderRepository = loginProviderRepository,
                        userDataRepository = userDataRepository,
                    )

                When("Apple 재인증이 필요한지 판정한다") {
                    val result = useCase(parameter = Unit)

                    Then("TC-1423165f-ffc3-4281-b7a0-13e4561f09ce 재인증 필요 여부가 $expected 다") {
                        result.shouldBeSuccess(expected)
                    }
                }
            }
        }

        Given("로그인 수단 기록을 읽지 못하도록 준비되어 있고 오류 보고 기록 수단이 등록되어 있다") {
            val failure = IllegalStateException("read failed")
            val loginProviderRepository = mockk<LoginProviderRepository>()
            coEvery { loginProviderRepository.read() } throws failure
            val useCase = CheckAppleReauthenticationRequiredUseCase(loginProviderRepository = loginProviderRepository, userDataRepository = mockk())
            val reportList = recordCrashlyticsLog()

            When("Apple 재인증이 필요한지 판정한다") {
                val result = useCase(parameter = Unit)

                Then("TC-0ad36304-87ed-4c7b-bf3c-34c2ff1b125e TC-364abafa-b866-4902-b128-c8a3799b563f 실패를 돌려주고 오류 보고는 남기지 않는다") {
                    result.shouldBeFailure()
                    reportList.shouldBeEmpty()
                }
            }
        }
    })
