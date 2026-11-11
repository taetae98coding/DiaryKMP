package io.github.taetae98coding.diary.domain.contact.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.contact.contact
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.contact.repository.AccountContactRepository
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant
import kotlin.uuid.Uuid

class FindContactUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 저장된 연락처가 준비되어 있다") {
            val account = Arb.accountUser().next()
            val contact = contact()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.success(account))
            val accountContactRepository = mockk<AccountContactRepository>()
            every { accountContactRepository.find(account = account, contactId = contact.id) } returns flowOf(contact)
            val useCase =
                FindContactUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountContactRepository = accountContactRepository,
                )

            When("대상 식별자로 연락처를 조회한다") {
                Then("TC-250658dc-32f8-47a3-8410-3e244df01331 현재 계정의 연락처를 전달한다") {
                    useCase(parameter = contact.id).first().shouldBeSuccess(contact)
                }
            }
        }

        Given("현재 계정과 연결되지 않은 식별자가 준비되어 있다") {
            val account = Arb.accountUser().next()
            val contactId = Uuid.random()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.success(account))
            val accountContactRepository = mockk<AccountContactRepository>()
            every { accountContactRepository.find(account = account, contactId = contactId) } returns flowOf(null)
            val useCase =
                FindContactUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountContactRepository = accountContactRepository,
                )

            When("대상 식별자로 연락처를 조회한다") {
                Then("TC-229c2151-ee96-48d8-b3fc-ba0dd5bf1723 조회되는 연락처가 없다") {
                    useCase(parameter = contactId).first().shouldBeSuccess(null)
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(Arb.string().next())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.failure(throwable))
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
        private fun contact(): Contact =
            Arb
                .contact()
                .next()
                .copy(
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                )
    }
}
