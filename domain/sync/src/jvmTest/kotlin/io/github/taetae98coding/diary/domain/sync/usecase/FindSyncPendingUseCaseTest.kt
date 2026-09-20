package io.github.taetae98coding.diary.domain.sync.usecase

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.sync.repository.AccountSyncPendingRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf

class FindSyncPendingUseCaseTest :
    BehaviorSpec({
        Given("사용자 계정에 업로드 대기 항목이 있다") {
            When("로그인 세션의 갱신 여부가 서로 다르다") {
                Then("TC-MORE-HOME-DOMAIN-006 세션 갱신 여부와 관계없이 대기 항목이 있다고 알린다") {
                    listOf(true, false).forEach { isSessionValid ->
                        val account = fixtureMonkey.giveMeOne<Account.User>().copy(isSessionValid = isSessionValid)
                        val getAccountUseCase = mockk<GetAccountUseCase>()
                        every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
                        val repository = mockk<AccountSyncPendingRepository>()
                        every { repository.find(account = account) } returns flowOf(true)
                        val useCase =
                            FindSyncPendingUseCase(
                                getAccountUseCase = getAccountUseCase,
                                accountSyncPendingRepository = repository,
                            )

                        useCase(parameter = Unit).test {
                            withClue("isSessionValid=$isSessionValid") { awaitItem().shouldBeSuccess(true) }
                            awaitComplete()
                        }
                    }
                }
            }

            When("업로드 대기 여부를 조회한다") {
                Then("TC-MORE-HOME-DATA-003 확인된 계정으로 기기 저장소를 한 번 조회한다") {
                    val account = fixtureMonkey.giveMeOne<Account.User>()
                    val getAccountUseCase = mockk<GetAccountUseCase>()
                    every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
                    val repository = mockk<AccountSyncPendingRepository>()
                    every { repository.find(account = account) } returns flowOf(true)
                    val useCase =
                        FindSyncPendingUseCase(
                            getAccountUseCase = getAccountUseCase,
                            accountSyncPendingRepository = repository,
                        )

                    useCase(parameter = Unit).test {
                        awaitItem().shouldBeSuccess(true)
                        awaitComplete()
                    }

                    verify(exactly = 1) { repository.find(account = account) }
                }
            }
        }

        Given("사용자 계정에 업로드 대기 항목이 없다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val repository = mockk<AccountSyncPendingRepository>()
            every { repository.find(account = account) } returns flowOf(false)
            val useCase =
                FindSyncPendingUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountSyncPendingRepository = repository,
                )

            When("업로드 대기 여부를 조회한다") {
                Then("대기 항목이 없다고 알린다") {
                    useCase(parameter = Unit).test {
                        awaitItem().shouldBeSuccess(false)
                        awaitComplete()
                    }
                }
            }
        }

        Given("기기 저장소의 업로드 대기 여부가 바뀐다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val repository = mockk<AccountSyncPendingRepository>()
            every { repository.find(account = account) } returns flowOf(true, false)
            val useCase =
                FindSyncPendingUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountSyncPendingRepository = repository,
                )

            When("업로드 대기 여부를 조회한다") {
                Then("바뀐 값을 이어서 알린다") {
                    useCase(parameter = Unit).test {
                        awaitItem().shouldBeSuccess(true)
                        awaitItem().shouldBeSuccess(false)
                        awaitComplete()
                    }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val repository = mockk<AccountSyncPendingRepository>()
            val useCase =
                FindSyncPendingUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountSyncPendingRepository = repository,
                )

            When("업로드 대기 여부를 조회한다") {
                Then("계정 조회 실패를 전달하고 기기 저장소를 조회하지 않는다") {
                    useCase(parameter = Unit).test {
                        awaitItem().shouldBeFailure() shouldBeSameInstanceAs throwable
                        awaitComplete()
                    }

                    verify(exactly = 0) { repository.find(account = any()) }
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
