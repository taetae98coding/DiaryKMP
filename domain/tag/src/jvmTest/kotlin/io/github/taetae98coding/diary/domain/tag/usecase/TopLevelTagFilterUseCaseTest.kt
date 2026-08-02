package io.github.taetae98coding.diary.domain.tag.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.tag.repository.AccountTagFilterRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

class TopLevelTagFilterUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))

            When("현재 계정에 켜진 필터 선택이 저장되어 있다") {
                val accountTagFilterRepository = mockk<AccountTagFilterRepository>()
                every { accountTagFilterRepository.getTopLevelOnly(account = account) } returns flowOf(true)
                val useCase =
                    GetTopLevelTagFilterUseCase(
                        getAccountUseCase = getAccountUseCase,
                        accountTagFilterRepository = accountTagFilterRepository,
                    )

                Then("TC-TAG-HOME-FEATURE-027 켜진 상태를 전달한다") {
                    useCase(parameter = Unit).first().shouldBeSuccess() shouldBe true
                }
            }

            When("현재 계정에 꺼진 필터 선택이 저장되어 있다") {
                val accountTagFilterRepository = mockk<AccountTagFilterRepository>()
                every { accountTagFilterRepository.getTopLevelOnly(account = account) } returns flowOf(false)
                val useCase =
                    GetTopLevelTagFilterUseCase(
                        getAccountUseCase = getAccountUseCase,
                        accountTagFilterRepository = accountTagFilterRepository,
                    )

                Then("TC-TAG-HOME-DATA-009 꺼진 상태를 전달한다") {
                    useCase(parameter = Unit).first().shouldBeSuccess() shouldBe false
                }
            }

            When("최상위 태그만 보기를 켠다") {
                val accountTagFilterRepository = mockk<AccountTagFilterRepository>(relaxed = true)
                val useCase =
                    EnableTopLevelTagFilterUseCase(
                        getAccountUseCase = getAccountUseCase,
                        accountTagFilterRepository = accountTagFilterRepository,
                    )

                Then("TC-TAG-HOME-FEATURE-028 현재 계정의 필터 선택을 켜진 상태로 저장한다") {
                    useCase(parameter = Unit).shouldBeSuccess()

                    coVerify(exactly = 1) { accountTagFilterRepository.upsert(account = account, isTopLevelOnly = true) }
                }
            }

            When("최상위 태그만 보기를 끈다") {
                val accountTagFilterRepository = mockk<AccountTagFilterRepository>(relaxed = true)
                val useCase =
                    DisableTopLevelTagFilterUseCase(
                        getAccountUseCase = getAccountUseCase,
                        accountTagFilterRepository = accountTagFilterRepository,
                    )

                Then("TC-TAG-HOME-FEATURE-029 현재 계정의 필터 선택을 꺼진 상태로 저장한다") {
                    useCase(parameter = Unit).shouldBeSuccess()

                    coVerify(exactly = 1) { accountTagFilterRepository.upsert(account = account, isTopLevelOnly = false) }
                }
            }
        }

        Given("계정 조회에 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))

            When("필터 선택을 조회한다") {
                val accountTagFilterRepository = mockk<AccountTagFilterRepository>(relaxed = true)
                val useCase =
                    GetTopLevelTagFilterUseCase(
                        getAccountUseCase = getAccountUseCase,
                        accountTagFilterRepository = accountTagFilterRepository,
                    )

                Then("계정 조회 실패를 전달하고 저장소를 조회하지 않는다") {
                    useCase(parameter = Unit)
                        .first()
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)
                }
            }

            When("최상위 태그만 보기를 켠다") {
                val accountTagFilterRepository = mockk<AccountTagFilterRepository>(relaxed = true)
                val useCase =
                    EnableTopLevelTagFilterUseCase(
                        getAccountUseCase = getAccountUseCase,
                        accountTagFilterRepository = accountTagFilterRepository,
                    )

                Then("계정 조회 실패를 전달하고 필터 선택을 저장하지 않는다") {
                    useCase(parameter = Unit)
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)

                    coVerify(exactly = 0) { accountTagFilterRepository.upsert(account = any(), isTopLevelOnly = any()) }
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
