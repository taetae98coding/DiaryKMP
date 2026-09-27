package io.github.taetae98coding.diary.domain.file.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class AwaitFileUploadAccountChangeUseCaseTest :
    BehaviorSpec({
        Given("계정 A로 올리기를 시작했다") {
            When("계정 상태가 게스트나 다른 계정의 사용자로 바뀐다") {
                Then("TC-FILE-STORAGE-DOMAIN-010 계정이 바뀐 것으로 보고 기다림을 끝낸다") {
                    listOf<Account>(Account.Guest, fixtureMonkey.giveMeOne<Account.User>()).forEach { changedAccount ->
                        runTest {
                            val account = fixtureMonkey.giveMeOne<Account.User>()
                            val accountFlow = MutableStateFlow<Result<Account>>(Result.success(account))
                            val useCase = AwaitFileUploadAccountChangeUseCase(getAccountUseCase = getAccountUseCase(accountFlow = accountFlow))

                            val result = async { useCase(parameter = account.id) }
                            runCurrent()
                            result.isCompleted shouldBe false

                            accountFlow.value = Result.success(changedAccount)

                            result.await().shouldBeSuccess()
                        }
                    }
                }
            }

            When("계정 A의 세션 갱신 여부만 바뀌거나 계정 상태를 확인하지 못한다") {
                Then("TC-FILE-STORAGE-DOMAIN-010 계정이 바뀐 것으로 보지 않고 계속 기다린다") {
                    listOf<(Account.User) -> Result<Account>>(
                        { account -> Result.success(account.copy(isSessionValid = !account.isSessionValid, isSessionPending = !account.isSessionPending)) },
                        { Result.failure(IllegalStateException(fixtureMonkey.giveMeOne<String>())) },
                    ).forEach { change ->
                        runTest {
                            val account = fixtureMonkey.giveMeOne<Account.User>()
                            val accountFlow = MutableStateFlow<Result<Account>>(Result.success(account))
                            val useCase = AwaitFileUploadAccountChangeUseCase(getAccountUseCase = getAccountUseCase(accountFlow = accountFlow))

                            val result = async { useCase(parameter = account.id) }
                            runCurrent()
                            accountFlow.value = change(account)
                            runCurrent()

                            result.isCompleted shouldBe false
                            result.cancel()
                        }
                    }
                }
            }
        }
    }) {
    public companion object {
        private fun getAccountUseCase(accountFlow: MutableStateFlow<Result<Account>>): GetAccountUseCase {
            val useCase = mockk<GetAccountUseCase>()
            every { useCase(parameter = Unit) } returns accountFlow
            return useCase
        }
    }
}
