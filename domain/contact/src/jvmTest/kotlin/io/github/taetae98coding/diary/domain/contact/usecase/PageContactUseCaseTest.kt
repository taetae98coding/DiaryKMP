package io.github.taetae98coding.diary.domain.contact.usecase

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.contact.repository.AccountContactRepository
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

class PageContactUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 계정의 연락처가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val contactList = List(2) { contact() }
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountContactRepository = mockk<AccountContactRepository>()
            every { accountContactRepository.page(account = account, sort = ListSort.NAME) } returns flowOf(PagingData.from(contactList))
            val useCase =
                PageContactUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountContactRepository = accountContactRepository,
                )

            When("연락처 목록을 페이지로 조회한다") {
                Then("TC-CONTACT-HOME-DOMAIN-001 TC-CONTACT-HOME-DATA-001 현재 계정의 연락처를 페이지로 전달한다") {
                    val pagingData = useCase(parameter = ListSort.NAME).first().shouldBeSuccess()

                    flowOf(pagingData).asSnapshot() shouldBe contactList
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountContactRepository = mockk<AccountContactRepository>(relaxed = true)
            val useCase =
                PageContactUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountContactRepository = accountContactRepository,
                )

            When("연락처 목록을 페이지로 조회한다") {
                Then("TC-CONTACT-HOME-DOMAIN-002 실패를 그대로 전달한다") {
                    useCase(parameter = ListSort.NAME)
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
