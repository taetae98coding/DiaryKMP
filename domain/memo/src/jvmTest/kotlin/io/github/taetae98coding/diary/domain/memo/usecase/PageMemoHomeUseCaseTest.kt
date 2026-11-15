package io.github.taetae98coding.diary.domain.memo.usecase

import androidx.paging.PagingData
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoRepository
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant

class PageMemoHomeUseCaseTest :
    BehaviorSpec({
        Given("현재 계정의 메모 페이지가 준비되어 있다") {
            val account = Arb.accountUser().next()
            val pagingData = PagingData.from(listOf(memo()))
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountMemoRepository = mockk<AccountMemoRepository>()
            every { getAccountUseCase() } returns flowOf(Result.success(account))
            every { accountMemoRepository.page(account = account, sort = ListSort.DEFAULT) } returns flowOf(pagingData)
            val useCase =
                PageMemoHomeUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMemoRepository = accountMemoRepository,
                )

            When("MemoHome 목록 페이지를 조회한다") {
                Then("현재 계정의 메모 페이지를 반환한다") {
                    useCase(parameter = ListSort.DEFAULT).test {
                        awaitItem().getOrThrow() shouldBeSameInstanceAs pagingData
                        awaitComplete()
                    }

                    verify(exactly = 1) {
                        accountMemoRepository.page(account = account, sort = ListSort.DEFAULT)
                    }
                }
            }
        }

        Given("현재 계정 조회에 실패하도록 준비되어 있다") {
            val failure = IllegalStateException(Arb.string().next())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            val accountMemoRepository = mockk<AccountMemoRepository>(relaxed = true)
            every { getAccountUseCase() } returns flowOf(Result.failure(failure))
            val useCase =
                PageMemoHomeUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMemoRepository = accountMemoRepository,
                )

            When("MemoHome 목록 페이지를 조회한다") {
                Then("계정 조회 실패를 전달하고 저장소에 페이지를 요청하지 않는다") {
                    useCase(parameter = ListSort.DEFAULT).test {
                        awaitItem().shouldBeFailure { throwable -> throwable shouldBe failure }
                        awaitComplete()
                    }

                    verify(exactly = 0) {
                        accountMemoRepository.page(account = any(), sort = any())
                    }
                }
            }
        }
    }) {
    public companion object {
        private fun memo(): Memo =
            Arb
                .memo()
                .next()
                .copy(
                    updatedAt = Arb.secondPrecisionInstant().next(),
                    createdAt = Arb.secondPrecisionInstant().next(),
                )
    }
}
