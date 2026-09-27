package io.github.taetae98coding.diary.domain.contact.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.contact.repository.AccountContactRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant
import kotlin.uuid.Uuid

class FindContactUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 저장된 연락처가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val contact = contact()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountContactRepository = mockk<AccountContactRepository>()
            every { accountContactRepository.find(account = account, contactId = contact.id) } returns flowOf(contact)
            val useCase =
                FindContactUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountContactRepository = accountContactRepository,
                )

            When("대상 식별자로 연락처를 조회한다") {
                Then("TC-CONTACT-DETAIL-DATA-001 현재 계정의 연락처를 전달한다") {
                    useCase(parameter = contact.id).first().shouldBeSuccess(contact)
                }
            }
        }

        Given("현재 계정과 연결되지 않은 식별자가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val contactId = Uuid.random()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountContactRepository = mockk<AccountContactRepository>()
            every { accountContactRepository.find(account = account, contactId = contactId) } returns flowOf(null)
            val useCase =
                FindContactUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountContactRepository = accountContactRepository,
                )

            When("대상 식별자로 연락처를 조회한다") {
                Then("TC-CONTACT-DETAIL-DATA-002 조회되는 연락처가 없다") {
                    useCase(parameter = contactId).first().shouldBeSuccess(null)
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountContactRepository = mockk<AccountContactRepository>(relaxed = true)
            val useCase =
                FindContactUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountContactRepository = accountContactRepository,
                )

            When("대상 식별자로 연락처를 조회한다") {
                Then("실패를 그대로 전달한다") {
                    useCase(parameter = Uuid.random())
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

        private fun contact(): Contact =
            fixtureMonkey
                .giveMeKotlinBuilder<Contact>()
                .setExp(Contact::updatedAt, fixtureMonkey.giveMeOne<Instant>())
                .setExp(Contact::createdAt, fixtureMonkey.giveMeOne<Instant>())
                .sample()
    }
}
