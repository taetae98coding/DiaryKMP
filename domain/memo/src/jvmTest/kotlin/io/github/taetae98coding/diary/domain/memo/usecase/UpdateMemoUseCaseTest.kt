package io.github.taetae98coding.diary.domain.memo.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class UpdateMemoUseCaseTest :
    BehaviorSpec({
        listOf(
            "" to "빈 제목",
            "   " to "공백 문자로만 이루어진 제목",
        ).forEach { (blankTitle, label) ->
            Given("$label 으로 수정하고 기존 메모에 저장된 제목이 있다") {
                val account = fixtureMonkey.giveMeOne<Account.User>()
                val now = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
                val memoId = fixtureMonkey.giveMeOne<Uuid>()
                val storedMemo =
                    fixtureMonkey
                        .giveMeKotlinBuilder<Memo>()
                        .setExp(Memo::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                        .setExp(Memo::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                        .sample()
                val detailSlot = slot<MemoDetail>()
                val getAccountUseCase = mockk<GetAccountUseCase>()
                every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
                val requestSyncUseCase = mockk<RequestSyncUseCase>()
                coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
                val findMemoUseCase = mockk<FindMemoUseCase>()
                every { findMemoUseCase(parameter = memoId) } returns flowOf(Result.success(storedMemo))
                val accountMemoRepository = mockk<AccountMemoRepository>()
                coEvery {
                    accountMemoRepository.updateDetail(account = account, memoId = memoId, detail = capture(detailSlot), updatedAt = any())
                } returns 1
                val clock = mockk<Clock>()
                every { clock.now() } returns now
                val useCase =
                    UpdateMemoUseCase(
                        getAccountUseCase = getAccountUseCase,
                        findMemoUseCase = findMemoUseCase,
                        requestSyncUseCase = requestSyncUseCase,
                        accountMemoRepository = accountMemoRepository,
                        clock = clock,
                    )

                When("설명, 컬러, 기간만 바꿔 수정한다") {
                    Then("TC-MEMO-DETAIL-DOMAIN-001 기존 제목을 유지하고 설명, 컬러, 기간은 수정한 내용으로 저장한다") {
                        val detail = fixtureMonkey.giveMeOne<MemoDetail>().copy(title = blankTitle)

                        val result = useCase(parameter = UpdateMemoUseCase.Parameter(id = memoId, detail = detail))

                        result.shouldBeSuccess(1)
                        detailSlot.captured.title shouldBe storedMemo.detail.title
                        detailSlot.captured.description shouldBe detail.description
                        detailSlot.captured.color shouldBe detail.color
                        detailSlot.captured.dateTime shouldBe detail.dateTime
                    }
                }
            }
        }

        Given("로그인한 계정과 현재 시각이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val now = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val memoId = fixtureMonkey.giveMeOne<Uuid>()
            val detailSlot = slot<MemoDetail>()
            val updatedAtSlot = slot<Instant>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val findMemoUseCase = mockk<FindMemoUseCase>()
            val accountMemoRepository = mockk<AccountMemoRepository>()
            coEvery {
                accountMemoRepository.updateDetail(
                    account = account,
                    memoId = memoId,
                    detail = capture(detailSlot),
                    updatedAt = capture(updatedAtSlot),
                )
            } returns 1
            val clock = mockk<Clock>()
            every { clock.now() } returns now
            val useCase =
                UpdateMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    findMemoUseCase = findMemoUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    clock = clock,
                )

            When("공백이 아닌 제목으로 메모를 수정한다") {
                Then("TC-MEMO-DETAIL-DATA-001 제목, 설명, 컬러와 저장 시각으로 현재 계정의 메모를 갱신한다") {
                    val detail = fixtureMonkey.giveMeOne<MemoDetail>().copy(title = "title-${fixtureMonkey.giveMeOne<String>()}")

                    val result = useCase(parameter = UpdateMemoUseCase.Parameter(id = memoId, detail = detail))

                    result.shouldBeSuccess(1)
                    coVerify(exactly = 1) {
                        accountMemoRepository.updateDetail(account = account, memoId = memoId, detail = any(), updatedAt = any())
                    }
                    detailSlot.captured shouldBe detail
                    updatedAtSlot.captured shouldBe now
                }

                Then("TC-MEMO-DETAIL-DATA-002 수정한 기간이 저장에 반영된다") {
                    listOf(
                        fixtureMonkey.giveMeOne<MemoDateTime.AllDay>(),
                        fixtureMonkey.giveMeOne<MemoDateTime.DateTime>(),
                        null,
                    ).forEach { dateTime ->
                        val detail =
                            fixtureMonkey.giveMeOne<MemoDetail>().copy(
                                title = "title-${fixtureMonkey.giveMeOne<String>()}",
                                dateTime = dateTime,
                            )

                        val result = useCase(parameter = UpdateMemoUseCase.Parameter(id = memoId, detail = detail))

                        result.shouldBeSuccess(1)
                        detailSlot.captured.dateTime shouldBe dateTime
                    }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            val findMemoUseCase = mockk<FindMemoUseCase>()
            val accountMemoRepository = mockk<AccountMemoRepository>(relaxed = true)
            val clock = mockk<Clock>(relaxed = true)
            val useCase =
                UpdateMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    findMemoUseCase = findMemoUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    clock = clock,
                )

            When("공백이 아닌 제목으로 메모를 수정한다") {
                Then("계정 조회 실패를 전달하고 수정하지 않는다") {
                    val parameter =
                        UpdateMemoUseCase.Parameter(
                            id = fixtureMonkey.giveMeOne<Uuid>(),
                            detail = fixtureMonkey.giveMeOne<MemoDetail>().copy(title = "title-${fixtureMonkey.giveMeOne<String>()}"),
                        )

                    val result = useCase(parameter = parameter)

                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                    coVerify(exactly = 0) {
                        accountMemoRepository.updateDetail(account = any(), memoId = any(), detail = any(), updatedAt = any())
                    }
                    coVerify(exactly = 0) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("메모 수정과 동기화 요청이 성공하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val memoId = fixtureMonkey.giveMeOne<Uuid>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val findMemoUseCase = mockk<FindMemoUseCase>()
            val accountMemoRepository = mockk<AccountMemoRepository>()
            coEvery {
                accountMemoRepository.updateDetail(account = account, memoId = memoId, detail = any(), updatedAt = any())
            } returns 1
            val clock = mockk<Clock>()
            every { clock.now() } returns Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val useCase =
                UpdateMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    findMemoUseCase = findMemoUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    clock = clock,
                )

            When("공백이 아닌 제목으로 메모를 수정한다") {
                Then("TC-SYNC-REFRESH-FEATURE-004 메모 갱신 후 동기화를 한 번 요청한다") {
                    val detail = fixtureMonkey.giveMeOne<MemoDetail>().copy(title = "title-${fixtureMonkey.giveMeOne<String>()}")

                    val result = useCase(parameter = UpdateMemoUseCase.Parameter(id = memoId, detail = detail))

                    result.shouldBeSuccess(1)
                    coVerifyOrder {
                        accountMemoRepository.updateDetail(account = account, memoId = memoId, detail = any(), updatedAt = any())
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
