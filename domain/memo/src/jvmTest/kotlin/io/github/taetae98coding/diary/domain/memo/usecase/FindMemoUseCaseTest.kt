package io.github.taetae98coding.diary.domain.memo.usecase

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.memo.memo
import io.github.taetae98coding.diary.core.testing.memo.memoDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoRepository
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant
import kotlin.uuid.Uuid

class FindMemoUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 조회할 수 있는 메모가 준비되어 있다") {
            val account = Arb.accountUser().next()
            val memo = memo()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.success(account))
            val accountMemoRepository = mockk<AccountMemoRepository>()
            every { accountMemoRepository.find(account = account, memoId = memo.id) } returns flowOf(memo)
            val useCase =
                FindMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMemoRepository = accountMemoRepository,
                )

            When("메모를 조회한다") {
                Then("현재 계정의 메모를 반환한다") {
                    val result = useCase(parameter = memo.id).first()

                    result.shouldBeSuccess() shouldBe memo
                    verify(exactly = 1) {
                        accountMemoRepository.find(account = account, memoId = memo.id)
                    }
                }
            }
        }

        Given("조회할 메모가 없다") {
            val account = Arb.accountUser().next()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.success(account))
            val accountMemoRepository = mockk<AccountMemoRepository>()
            every { accountMemoRepository.find(account = account, memoId = any()) } returns flowOf(null)
            val useCase =
                FindMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMemoRepository = accountMemoRepository,
                )

            When("메모를 조회한다") {
                Then("메모 없음을 반환한다") {
                    val result = useCase(parameter = Arb.kotlinUuid().next()).first()

                    result.shouldBeSuccess().shouldBeNull()
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(Arb.string().next())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.failure(throwable))
            val accountMemoRepository = mockk<AccountMemoRepository>(relaxed = true)
            val useCase =
                FindMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMemoRepository = accountMemoRepository,
                )

            When("메모 조회를 실행한다") {
                Then("계정 조회 실패를 전달하고 메모를 조회하지 않는다") {
                    val result = useCase(parameter = Arb.kotlinUuid().next()).first()

                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                    verify(exactly = 0) {
                        accountMemoRepository.find(account = any(), memoId = any())
                    }
                }
            }
        }

        Given("조회한 메모가 변경되는 Flow가 준비되어 있다") {
            val account = Arb.accountUser().next()
            val memo = memo()
            val changedMemo = memo.copy(detail = Arb.memoDetail().next())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.success(account))
            val memoFlow = MutableStateFlow<Memo?>(memo)
            val accountMemoRepository = mockk<AccountMemoRepository>()
            every { accountMemoRepository.find(account = account, memoId = memo.id) } returns memoFlow
            val useCase =
                FindMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMemoRepository = accountMemoRepository,
                )

            When("메모가 변경된다") {
                Then("변경된 메모를 이어서 반환한다") {
                    useCase(parameter = memo.id).test {
                        awaitItem().shouldBeSuccess() shouldBe memo

                        memoFlow.value = changedMemo

                        awaitItem().shouldBeSuccess() shouldBe changedMemo
                        cancelAndIgnoreRemainingEvents()
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
