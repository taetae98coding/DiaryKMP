package io.github.taetae98coding.diary.domain.memo.usecase

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoContactRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant
import kotlin.uuid.Uuid

class PageMemoSelectableContactUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 선택할 수 있는 연락처가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val contactList = List(2) { contact() }
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountMemoContactRepository = mockk<AccountMemoContactRepository>()
            every {
                accountMemoContactRepository.pageSelectableContact(account = account, query = any())
            } returns flowOf(PagingData.from(contactList))
            val useCase =
                PageMemoSelectableContactUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMemoContactRepository = accountMemoContactRepository,
                )

            When("TC-MEMO-CONTACT-INPUT-DATA-003 검색어로 선택 목록을 조회한다") {
                Then("앞뒤 공백을 뺀 검색어로 조회하고 그 결과를 페이지로 전달한다") {
                    val pagingData =
                        useCase(parameter = "  Wiki  ")
                            .first()
                            .shouldBeSuccess()

                    flowOf(pagingData).asSnapshot() shouldBe contactList
                    verify(exactly = 1) {
                        accountMemoContactRepository.pageSelectableContact(account = account, query = "Wiki")
                    }
                }
            }

            When("TC-MEMO-CONTACT-INPUT-DOMAIN-010 공백만 있는 검색어로 선택 목록을 조회한다") {
                Then("검색어가 없는 것과 같게 조회한다") {
                    useCase(parameter = "   ")
                        .first()
                        .shouldBeSuccess()

                    verify(exactly = 1) {
                        accountMemoContactRepository.pageSelectableContact(account = account, query = "")
                    }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountMemoContactRepository = mockk<AccountMemoContactRepository>(relaxed = true)
            val useCase =
                PageMemoSelectableContactUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMemoContactRepository = accountMemoContactRepository,
                )

            When("선택 목록을 조회한다") {
                Then("실패를 그대로 전달한다") {
                    useCase(parameter = "")
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
                .setExp(Contact::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Contact::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
