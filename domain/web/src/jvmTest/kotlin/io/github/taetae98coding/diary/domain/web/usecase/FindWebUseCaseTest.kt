package io.github.taetae98coding.diary.domain.web.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.web.repository.AccountWebRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant
import kotlin.uuid.Uuid

class FindWebUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 그 계정의 웹 항목이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val web = web()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountWebRepository = mockk<AccountWebRepository>()
            every { accountWebRepository.find(account = account, webId = web.id) } returns flowOf(web)
            val useCase =
                FindWebUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountWebRepository = accountWebRepository,
                )

            When("상세 대상 웹 항목을 조회한다") {
                Then("TC-WEB-DETAIL-DATA-001 현재 계정의 웹 항목을 전달한다") {
                    useCase(parameter = web.id).first().shouldBeSuccess() shouldBe web
                }
            }
        }

        Given("상세 대상이 될 수 없는 웹 항목이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val webId = fixtureMonkey.giveMeOne<Uuid>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountWebRepository = mockk<AccountWebRepository>()
            every { accountWebRepository.find(account = account, webId = webId) } returns flowOf(null)
            val useCase =
                FindWebUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountWebRepository = accountWebRepository,
                )

            When("상세 대상 웹 항목을 조회한다") {
                Then("TC-WEB-DETAIL-DOMAIN-001 조회되는 웹 항목이 없다") {
                    useCase(parameter = webId).first().shouldBeSuccess().shouldBeNull()
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountWebRepository = mockk<AccountWebRepository>(relaxed = true)
            val useCase =
                FindWebUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountWebRepository = accountWebRepository,
                )

            When("상세 대상 웹 항목을 조회한다") {
                Then("TC-WEB-DETAIL-DOMAIN-002 실패를 그대로 전달한다") {
                    useCase(parameter = fixtureMonkey.giveMeOne<Uuid>())
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
