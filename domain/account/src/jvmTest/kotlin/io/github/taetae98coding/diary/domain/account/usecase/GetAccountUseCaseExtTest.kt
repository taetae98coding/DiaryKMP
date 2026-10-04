package io.github.taetae98coding.diary.domain.account.usecase

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlin.uuid.Uuid

class GetAccountUseCaseExtTest :
    BehaviorSpec({
        Given("계정 확인이 사용자로 성공한다") {
            val account = userAccount()
            val useCase = getAccountUseCase(flowOf(Result.success(account)))

            When("계정에 이어 값을 관찰한다") {
                Then("확인한 계정으로 이어 붙인 값이 전달된다") {
                    useCase.flatMapAccount { value -> flowOf(Result.success(value)) }.test {
                        awaitItem().shouldBeSuccess(account)
                        awaitComplete()
                    }
                }
            }

            When("계정을 한 번 확인한다") {
                Then("확인한 계정이 반환된다") {
                    useCase.requireAccount() shouldBe account
                }
            }
        }

        Given("계정 확인이 실패한다") {
            val failure = IllegalStateException("account error")
            val useCase = getAccountUseCase(flowOf(Result.failure(failure)))

            When("계정에 이어 값을 관찰한다") {
                Then("이어 붙일 흐름을 만들지 않고 같은 실패가 전달된다") {
                    var transformCount = 0

                    useCase
                        .flatMapAccount { value ->
                            transformCount++
                            flowOf(Result.success(value))
                        }.test {
                            awaitItem().shouldBeFailure { throwable -> throwable shouldBeSameInstanceAs failure }
                            awaitComplete()
                        }
                    transformCount shouldBe 0
                }
            }

            When("계정을 한 번 확인한다") {
                Then("같은 실패가 던져진다") {
                    shouldThrow<IllegalStateException> { useCase.requireAccount() } shouldBeSameInstanceAs failure
                }
            }
        }

        Given("계정을 관찰하는 중에 다른 계정으로 바뀐다") {
            val first = userAccount()
            val second = userAccount()
            val accountFlow = MutableStateFlow<Result<Account>>(Result.success(first))
            val useCase = getAccountUseCase(accountFlow)

            When("계정에 이어 값을 관찰한다") {
                Then("이전 계정의 흐름을 취소하고 바뀐 계정의 값만 전달된다") {
                    val sourceMap = mutableMapOf<Account, MutableSharedFlow<String>>()
                    val cancelledList = mutableListOf<Account>()

                    useCase
                        .flatMapAccount { account ->
                            sourceFlow(account = account, sourceMap = sourceMap, cancelledList = cancelledList)
                        }.test {
                            sourceMap.getValue(first).emit("first")
                            awaitItem().shouldBeSuccess("first")

                            accountFlow.value = Result.success(second)
                            sourceMap.getValue(second).emit("second")
                            awaitItem().shouldBeSuccess("second")
                            cancelledList shouldBe listOf(first)

                            sourceMap.getValue(first).emit("stale")
                            expectNoEvents()
                        }
                }
            }
        }

        Given("계정을 관찰하는 중에 계정 확인이 실패로 바뀐다") {
            val account = userAccount()
            val failure = IllegalStateException("account error")
            val accountFlow = MutableStateFlow<Result<Account>>(Result.success(account))
            val useCase = getAccountUseCase(accountFlow)

            When("계정에 이어 값을 관찰한다") {
                Then("이전 계정의 흐름을 취소하고 실패가 전달된다") {
                    val sourceMap = mutableMapOf<Account, MutableSharedFlow<String>>()
                    val cancelledList = mutableListOf<Account>()

                    useCase
                        .flatMapAccount { value ->
                            sourceFlow(account = value, sourceMap = sourceMap, cancelledList = cancelledList)
                        }.test {
                            sourceMap.getValue(account).emit("value")
                            awaitItem().shouldBeSuccess("value")

                            accountFlow.value = Result.failure(failure)
                            awaitItem().shouldBeFailure { throwable -> throwable shouldBeSameInstanceAs failure }
                            cancelledList shouldBe listOf(account)
                        }
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

        private fun getAccountUseCase(accountFlow: Flow<Result<Account>>): GetAccountUseCase =
            mockk<GetAccountUseCase>().also { useCase ->
                every { useCase(Unit) } returns accountFlow
            }

        private fun sourceFlow(
            account: Account,
            sourceMap: MutableMap<Account, MutableSharedFlow<String>>,
            cancelledList: MutableList<Account>,
        ): Flow<Result<String>> {
            val source = MutableSharedFlow<String>()
            sourceMap[account] = source

            return source
                .map { value -> Result.success(value) }
                .onCompletion { cancelledList += account }
        }
    }
}
