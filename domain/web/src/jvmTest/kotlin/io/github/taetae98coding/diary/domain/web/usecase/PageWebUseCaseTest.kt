package io.github.taetae98coding.diary.domain.web.usecase

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.web.repository.AccountWebRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant

class PageWebUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 계정의 웹 항목이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val webList = List(2) { web() }
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountWebRepository = mockk<AccountWebRepository>()
            every { accountWebRepository.page(account = account, sort = ListSort.TITLE) } returns flowOf(PagingData.from(webList))
            val useCase =
                PageWebUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountWebRepository = accountWebRepository,
                )

            When("웹 목록을 페이지로 조회한다") {
                Then("TC-4bd8debd-a708-44a1-a8c6-ed7ec28f4b0d TC-15ca546d-a9aa-4c94-b533-83e2ed65763b 현재 계정의 웹 항목을 페이지로 전달한다") {
                    val pagingData = useCase(parameter = ListSort.TITLE).first().shouldBeSuccess()

                    flowOf(pagingData).asSnapshot() shouldBe webList
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountWebRepository = mockk<AccountWebRepository>(relaxed = true)
            val useCase =
                PageWebUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountWebRepository = accountWebRepository,
                )

            When("웹 목록을 페이지로 조회한다") {
                Then("TC-0e3aa805-7ba4-44cb-86c6-e816fedca70d 실패를 그대로 전달한다") {
                    useCase(parameter = ListSort.TITLE)
                        .first()
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun web(): Web =
            fixtureMonkey
                .giveMeKotlinBuilder<Web>()
                .setExp(Web::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Web::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()
    }
}
