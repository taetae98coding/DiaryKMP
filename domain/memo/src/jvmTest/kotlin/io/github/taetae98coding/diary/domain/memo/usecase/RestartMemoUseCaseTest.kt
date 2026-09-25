package io.github.taetae98coding.diary.domain.memo.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class RestartMemoUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 현재 시각이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val now = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val accountMemoRepository = mockk<AccountMemoRepository>()
            coEvery {
                accountMemoRepository.updateFinished(account = account, memoId = any(), isFinished = any(), updatedAt = any())
            } returns 1
            val clock = mockk<Clock>()
            every { clock.now() } returns now
            val useCase =
                RestartMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    clock = clock,
                )

            When("완료를 실행 취소한다") {
                Then("TC-MEMO-HOME-DOMAIN-003 TC-MEMO-HOME-DOMAIN-005 TC-MEMO-FINISHED-LIST-DOMAIN-001 TC-MEMO-FINISHED-LIST-DOMAIN-005 TC-TAG-MEMO-FINISHED-LIST-DOMAIN-002 현재 계정의 메모를 미완료 상태와 동작 시점 수정 시각으로 되돌린다") {
                    val memoId = fixtureMonkey.giveMeOne<Uuid>()

                    val result = useCase(parameter = memoId)

                    result.shouldBeSuccess(1)
                    coVerify(exactly = 1) {
                        accountMemoRepository.updateFinished(account = account, memoId = memoId, isFinished = false, updatedAt = now)
                    }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            val accountMemoRepository = mockk<AccountMemoRepository>(relaxed = true)
            val clock = mockk<Clock>(relaxed = true)
            val useCase =
                RestartMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    clock = clock,
                )

            When("완료 실행 취소를 실행한다") {
                Then("계정 조회 실패를 전달하고 메모 상태를 변경하지 않는다") {
                    val result = useCase(parameter = fixtureMonkey.giveMeOne<Uuid>())

                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                    coVerify(exactly = 0) {
                        accountMemoRepository.updateFinished(account = any(), memoId = any(), isFinished = any(), updatedAt = any())
                    }
                    coVerify(exactly = 0) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("완료 실행 취소와 동기화 요청이 성공하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val accountMemoRepository = mockk<AccountMemoRepository>()
            coEvery {
                accountMemoRepository.updateFinished(account = account, memoId = any(), isFinished = any(), updatedAt = any())
            } returns 1
            val clock = mockk<Clock>()
            every { clock.now() } returns Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val useCase =
                RestartMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    clock = clock,
                )

            When("완료를 실행 취소한다") {
                Then("TC-SYNC-REFRESH-FEATURE-004 메모 갱신 후 동기화를 한 번 요청한다") {
                    val result = useCase(parameter = fixtureMonkey.giveMeOne<Uuid>())

                    result.shouldBeSuccess(1)
                    coVerifyOrder {
                        accountMemoRepository.updateFinished(account = account, memoId = any(), isFinished = false, updatedAt = any())
                        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                    }
                    coVerify(exactly = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
