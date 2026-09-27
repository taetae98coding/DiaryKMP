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
import io.kotest.matchers.result.shouldBeSuccess
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant

class ContactDetailAccountUseCaseTest :
    BehaviorSpec({
        Given("삭제 상태인 연락처가 현재 계정과 연결되어 저장되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val deleted = contact().copy(isDeleted = true)
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountContactRepository = mockk<AccountContactRepository>()
            every { accountContactRepository.find(account = account, contactId = deleted.id) } returns flowOf(deleted)
            val useCase =
                FindContactUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountContactRepository = accountContactRepository,
                )

            When("그 연락처를 상세 대상으로 조회한다") {
                Then("TC-CONTACT-DETAIL-DOMAIN-001 삭제 상태인 연락처도 그대로 조회된다") {
                    useCase(parameter = deleted.id).first().shouldBeSuccess(deleted)
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
