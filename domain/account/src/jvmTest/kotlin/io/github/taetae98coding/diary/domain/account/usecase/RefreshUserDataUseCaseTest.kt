package io.github.taetae98coding.diary.domain.account.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.repository.UserDataRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.flowOf
import kotlin.uuid.Uuid

class RefreshUserDataUseCaseTest :
    BehaviorSpec({
        Given("계정이 사용자 상태이고 사용자 정보 다시 받기가 성공한다") {
            val repository = mockk<UserDataRepository>()
            coEvery { repository.refresh() } just runs
            val useCase = RefreshUserDataUseCase(getAccountUseCase = getAccountUseCase(Result.success(userAccount())), userDataRepository = repository)

            When("사용자 정보 다시 확인을 시작한다") {
                val result = useCase(Unit)

                Then("TC-MORE-HOME-FEATURE-032 저장된 사용자 정보를 다시 받는 요청이 한 번 이루어지고 성공이 전달된다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 1) { repository.refresh() }
                }
            }
        }

        listOf(
            "게스트" to Result.success<Account>(Account.Guest),
            "확인 실패" to Result.failure(IllegalStateException("account error")),
        ).forEach { (name, accountResult) ->
            Given("계정 상태가 $name 이다") {
                val repository = mockk<UserDataRepository>()
                coEvery { repository.refresh() } just runs
                val useCase = RefreshUserDataUseCase(getAccountUseCase = getAccountUseCase(accountResult), userDataRepository = repository)

                When("사용자 정보 다시 확인을 시작한다") {
                    val result = useCase(Unit)

                    Then("TC-MORE-HOME-DOMAIN-012 $name 저장된 사용자 정보를 다시 받는 요청이 이루어지지 않는다") {
                        result.shouldBeSuccess()
                        coVerify(exactly = 0) { repository.refresh() }
                    }
                }
            }
        }

        Given("계정이 사용자 상태이고 사용자 정보 다시 받기가 실패한다") {
            val repository = mockk<UserDataRepository>()
            coEvery { repository.refresh() } throws IllegalStateException("refresh failed")
            val useCase = RefreshUserDataUseCase(getAccountUseCase = getAccountUseCase(Result.success(userAccount())), userDataRepository = repository)

            When("사용자 정보 다시 확인을 시작한다") {
                val result = useCase(Unit)

                Then("TC-MORE-HOME-DOMAIN-013 실패가 그대로 전달된다") {
                    result.shouldBeFailure()
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun userAccount(): Account.User =
            Account.User(
                id = fixtureMonkey.giveMeOne<Uuid>(),
                email = fixtureMonkey.giveMeOne<String>(),
                profileImage = fixtureMonkey.giveMeOne<String>(),
                isSessionValid = fixtureMonkey.giveMeOne<Boolean>(),
            )

        private fun getAccountUseCase(result: Result<Account>): GetAccountUseCase =
            mockk<GetAccountUseCase>().also { useCase ->
                every { useCase(Unit) } returns flowOf(result)
            }
    }
}
