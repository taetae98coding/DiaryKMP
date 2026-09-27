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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
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

        Given("계정 정보를 아직 받지 못해 확인 중이 계속된다") {
            val repository = mockk<UserDataRepository>()
            coEvery { repository.refresh() } just runs
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(Unit) } returns flow { awaitCancellation() }
            val useCase = RefreshUserDataUseCase(getAccountUseCase = getAccountUseCase, userDataRepository = repository)

            When("사용자 정보 다시 확인을 시작한다") {
                Then("TC-MORE-HOME-DOMAIN-012 아직 받지 못함 저장된 사용자 정보를 다시 받는 요청이 이루어지지 않는다") {
                    runTest {
                        backgroundScope.launch { useCase(Unit) }
                        advanceUntilIdle()

                        coVerify(exactly = 0) { repository.refresh() }
                    }
                }
            }
        }

        listOf(
            Triple("사용자로 정해짐", { Result.success<Account>(userAccount()) }, 1),
            Triple("게스트로 정해짐", { Result.success<Account>(Account.Guest) }, 0),
            Triple("확인에 실패함", { Result.failure<Account>(IllegalStateException("account error")) }, 0),
        ).forEach { (name, accountResultOf, expectedRefreshCount) ->
            Given("계정 정보를 아직 받지 못해 확인 중인 상태에서 다시 확인을 시작했고 뒤에 $name") {
                val repository = mockk<UserDataRepository>()
                coEvery { repository.refresh() } just runs
                val accountResult = CompletableDeferred<Result<Account>>()
                val getAccountUseCase = mockk<GetAccountUseCase>()
                every { getAccountUseCase(Unit) } returns flow { emit(accountResult.await()) }
                val useCase = RefreshUserDataUseCase(getAccountUseCase = getAccountUseCase, userDataRepository = repository)

                When("기다리던 계정 정보 확인이 $name") {
                    Then("TC-MORE-HOME-DOMAIN-017 $name 저장된 사용자 정보를 다시 받는 요청이 ${expectedRefreshCount}번 이루어진다") {
                        runTest {
                            val refresh = backgroundScope.launch { useCase(Unit) }
                            advanceUntilIdle()
                            coVerify(exactly = 0) { repository.refresh() }

                            accountResult.complete(accountResultOf())
                            refresh.join()

                            coVerify(exactly = expectedRefreshCount) { repository.refresh() }
                        }
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
