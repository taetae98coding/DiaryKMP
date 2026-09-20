package io.github.taetae98coding.diary.domain.memo.usecase

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.memo.DailyMemo
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountDailyMemoRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class GetDailyMemoUseCaseTest :
    BehaviorSpec({
        Given("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-012: 두 계정에 각각 오늘의 메모가 저장되어 있고 현재 계정은 첫 번째 계정이다") {
            val date = fixtureMonkey.giveMeOne<LocalDate>()
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val otherAccount = fixtureMonkey.giveMeOne<Account.User>()
            val memoList = List(size = 2) { fixtureMonkey.giveMeOne<DailyMemo>() }
            val otherMemoList = List(size = 1) { fixtureMonkey.giveMeOne<DailyMemo>() }
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountDailyMemoRepository = mockk<AccountDailyMemoRepository>()
            every { accountDailyMemoRepository.get(account = account, date = date) } returns flowOf(memoList)
            every { accountDailyMemoRepository.get(account = otherAccount, date = date) } returns flowOf(otherMemoList)
            val useCase =
                GetDailyMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountDailyMemoRepository = accountDailyMemoRepository,
                )

            When("그 날의 오늘의 메모를 조회한다") {
                Then("첫 번째 계정과 연결된 메모만 결과에 포함된다") {
                    useCase(parameter = date).first().shouldBeSuccess() shouldBe memoList

                    verify(exactly = 1) { accountDailyMemoRepository.get(account = account, date = date) }
                    verify(exactly = 0) { accountDailyMemoRepository.get(account = otherAccount, date = any()) }
                }
            }
        }

        Given("현재 사용자 계정이 바뀔 수 있다") {
            val date = fixtureMonkey.giveMeOne<LocalDate>()
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val otherAccount = fixtureMonkey.giveMeOne<Account.User>()
            val memoList = List(size = 2) { fixtureMonkey.giveMeOne<DailyMemo>() }
            val otherMemoList = List(size = 1) { fixtureMonkey.giveMeOne<DailyMemo>() }
            val accountFlow = MutableStateFlow<Result<Account>>(Result.success(account))
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns accountFlow
            val accountDailyMemoRepository = mockk<AccountDailyMemoRepository>()
            every { accountDailyMemoRepository.get(account = account, date = date) } returns flowOf(memoList)
            every { accountDailyMemoRepository.get(account = otherAccount, date = date) } returns flowOf(otherMemoList)
            val useCase =
                GetDailyMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountDailyMemoRepository = accountDailyMemoRepository,
                )

            When("조회 중에 현재 사용자 계정이 바뀐다") {
                Then("바뀐 계정을 기준으로 오늘의 메모가 다시 정해진다") {
                    useCase(parameter = date).test {
                        awaitItem().shouldBeSuccess() shouldBe memoList

                        accountFlow.value = Result.success(otherAccount)

                        awaitItem().shouldBeSuccess() shouldBe otherMemoList
                    }
                }
            }
        }

        Given("TC-DAILY-MEMO-NOTIFICATION-DATA-002: 기기에 저장된 메모를 조회하면 실패하도록 준비되어 있다") {
            val date = fixtureMonkey.giveMeOne<LocalDate>()
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val throwable = IllegalStateException("query error")
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountDailyMemoRepository = mockk<AccountDailyMemoRepository>()
            every { accountDailyMemoRepository.get(account = account, date = date) } returns flow { throw throwable }
            val useCase =
                GetDailyMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountDailyMemoRepository = accountDailyMemoRepository,
                )

            When("오늘의 메모를 조회한다") {
                Then("빈 결과가 아니라 실패가 전달된다") {
                    val failure = useCase(parameter = date).first().shouldBeFailure()

                    // 코루틴이 스택 추적 복원을 위해 예외를 복사할 수 있어 같은 인스턴스가 아니라 종류와 메시지로 비교한다.
                    failure.shouldBeInstanceOf<IllegalStateException>()
                    failure.message shouldBe throwable.message
                }
            }
        }

        Given("현재 사용자 계정을 확인하지 못한다") {
            val date = fixtureMonkey.giveMeOne<LocalDate>()
            val throwable = IllegalStateException("account error")
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountDailyMemoRepository = mockk<AccountDailyMemoRepository>()
            val useCase =
                GetDailyMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountDailyMemoRepository = accountDailyMemoRepository,
                )

            When("오늘의 메모를 조회한다") {
                Then("계정 확인 실패가 그대로 전달되고 메모를 조회하지 않는다") {
                    useCase(parameter = date).first().shouldBeFailure() shouldBeSameInstanceAs throwable

                    verify(exactly = 0) { accountDailyMemoRepository.get(account = any(), date = any()) }
                }
            }
        }
    })
