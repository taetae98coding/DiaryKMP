package io.github.taetae98coding.diary.domain.memo.usecase

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoFilterRepository
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlin.uuid.Uuid

class GetMemoFilterTagIdUseCaseTest :
    BehaviorSpec({
        Given("현재 계정에 저장된 필터 선택이 준비되어 있다") {
            val account = Arb.accountUser().next()
            val tagIdSet = setOf(Arb.kotlinUuid().next(), Arb.kotlinUuid().next())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountMemoFilterRepository = mockk<AccountMemoFilterRepository>()
            every { getAccountUseCase() } returns flowOf(Result.success(account))
            every { accountMemoFilterRepository.getTagIdSet(account = account) } returns flowOf(tagIdSet)
            val useCase =
                GetMemoFilterTagIdUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMemoFilterRepository = accountMemoFilterRepository,
                )

            When("저장된 필터 선택을 조회한다") {
                Then("현재 계정에 저장된 선택 태그 식별자를 반환한다") {
                    useCase().test {
                        awaitItem().getOrThrow() shouldBe tagIdSet
                        awaitComplete()
                    }

                    verify(exactly = 1) {
                        accountMemoFilterRepository.getTagIdSet(account = account)
                    }
                }
            }
        }

        Given("현재 계정 조회에 실패하도록 준비되어 있다") {
            val failure = IllegalStateException(Arb.string().next())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountMemoFilterRepository = mockk<AccountMemoFilterRepository>(relaxed = true)
            every { getAccountUseCase() } returns flowOf(Result.failure(failure))
            val useCase =
                GetMemoFilterTagIdUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMemoFilterRepository = accountMemoFilterRepository,
                )

            When("저장된 필터 선택을 조회한다") {
                Then("계정 조회 실패를 전달하고 저장소에 선택을 요청하지 않는다") {
                    useCase().test {
                        awaitItem().shouldBeFailure { throwable -> throwable shouldBe failure }
                        awaitComplete()
                    }

                    verify(exactly = 0) {
                        accountMemoFilterRepository.getTagIdSet(account = any())
                    }
                }
            }
        }
    })
