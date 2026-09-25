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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class MoveMemoUseCaseTest :
    BehaviorSpec({
        Given("삭제되지 않은 메모가 저장되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val now = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val memoId = fixtureMonkey.giveMeOne<Uuid>()
            val storedMemo = memo(id = memoId, isDeleted = false)
            val detailSlot = slot<MemoDetail>()
            val updatedAtSlot = slot<Instant>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val findMemoUseCase = mockk<FindMemoUseCase>()
            every { findMemoUseCase(parameter = memoId) } returns flowOf(Result.success(storedMemo))
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
                MoveMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    findMemoUseCase = findMemoUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    clock = clock,
                )

            When("종일 기간을 새 날짜 범위로 이동한다") {
                Then("TC-CALENDAR-MEMO-MOVE-DATA-001 옮겨진 기간과 수정 시각이 저장되어 기존 값을 덮어쓴다") {
                    val dateTime = MemoDateTime.AllDay(dateRange = july(day = 14)..july(day = 16))
                    val dateRange = july(day = 21)..july(day = 23)

                    val result = useCase(parameter = MoveMemoUseCase.Parameter(id = memoId, fromDateTime = dateTime, toDateRange = dateRange))

                    result.shouldBeSuccess(Unit)
                    detailSlot.captured.dateTime shouldBe MemoDateTime.AllDay(dateRange = july(day = 21)..july(day = 23))
                    updatedAtSlot.captured shouldBe now
                }

                Then("TC-CALENDAR-MEMO-MOVE-DOMAIN-005 기간 외의 저장된 내용은 바뀌지 않는다") {
                    val dateTime = MemoDateTime.AllDay(dateRange = july(day = 14)..july(day = 16))
                    val dateRange = july(day = 16)..july(day = 18)

                    val result = useCase(parameter = MoveMemoUseCase.Parameter(id = memoId, fromDateTime = dateTime, toDateRange = dateRange))

                    result.shouldBeSuccess(Unit)
                    detailSlot.captured.title shouldBe storedMemo.detail.title
                    detailSlot.captured.description shouldBe storedMemo.detail.description
                    detailSlot.captured.color shouldBe storedMemo.detail.color
                    coVerify(exactly = 0) { accountMemoRepository.updateFinished(account = any(), memoId = any(), isFinished = any(), updatedAt = any()) }
                    coVerify(exactly = 0) { accountMemoRepository.updateDeleted(account = any(), memoId = any(), isDeleted = any(), updatedAt = any()) }
                }

                Then("TC-CALENDAR-MEMO-MOVE-DOMAIN-003 저장된 기간과 다르더라도 파라미터로 받은 이동 시작 시점의 기간을 기준으로 저장한다") {
                    val dateTime = MemoDateTime.AllDay(dateRange = july(day = 14)..july(day = 16))
                    val dateRange = july(day = 21)..july(day = 23)

                    val result = useCase(parameter = MoveMemoUseCase.Parameter(id = memoId, fromDateTime = dateTime, toDateRange = dateRange))

                    result.shouldBeSuccess(Unit)
                    detailSlot.captured.dateTime shouldBe MemoDateTime.AllDay(dateRange = july(day = 21)..july(day = 23))
                }

                Then("TC-SYNC-REFRESH-FEATURE-004 TC-CALENDAR-MEMO-MOVE-DATA-003 저장 후 동기화를 한 번 요청한다") {
                    val dateTime = MemoDateTime.AllDay(dateRange = july(day = 14)..july(day = 16))
                    val dateRange = july(day = 15)..july(day = 17)

                    val result = useCase(parameter = MoveMemoUseCase.Parameter(id = memoId, fromDateTime = dateTime, toDateRange = dateRange))

                    result.shouldBeSuccess(Unit)
                    coVerifyOrder {
                        accountMemoRepository.updateDetail(account = account, memoId = memoId, detail = any(), updatedAt = any())
                        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                    }
                }
            }

            When("시각이 있는 기간을 새 날짜 범위로 이동한다") {
                Then("TC-CALENDAR-MEMO-MOVE-DOMAIN-002 시각을 그대로 유지한 채 날짜만 바뀐다") {
                    val dateTime =
                        MemoDateTime.DateTime(
                            start = LocalDateTime(date = july(day = 14), time = LocalTime(hour = 10, minute = 0)),
                            endInclusive = LocalDateTime(date = july(day = 14), time = LocalTime(hour = 11, minute = 30)),
                        )
                    val dateRange = july(day = 16)..july(day = 16)

                    val result = useCase(parameter = MoveMemoUseCase.Parameter(id = memoId, fromDateTime = dateTime, toDateRange = dateRange))

                    result.shouldBeSuccess(Unit)
                    detailSlot.captured.dateTime shouldBe
                        MemoDateTime.DateTime(
                            start = LocalDateTime(date = july(day = 16), time = LocalTime(hour = 10, minute = 0)),
                            endInclusive = LocalDateTime(date = july(day = 16), time = LocalTime(hour = 11, minute = 30)),
                        )
                }
            }
        }

        Given("완료된 메모가 저장되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val memoId = fixtureMonkey.giveMeOne<Uuid>()
            val storedMemo = memo(id = memoId, isDeleted = false, isFinished = true)
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val findMemoUseCase = mockk<FindMemoUseCase>()
            every { findMemoUseCase(parameter = memoId) } returns flowOf(Result.success(storedMemo))
            val accountMemoRepository = mockk<AccountMemoRepository>()
            coEvery {
                accountMemoRepository.updateDetail(account = account, memoId = memoId, detail = any(), updatedAt = any())
            } returns 1
            val clock = mockk<Clock>()
            every { clock.now() } returns Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val useCase =
                MoveMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    findMemoUseCase = findMemoUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    clock = clock,
                )

            When("새 날짜 범위로 이동한다") {
                Then("TC-CALENDAR-MEMO-MOVE-DOMAIN-001 완료된 메모도 기간이 저장되고 완료 상태는 바꾸지 않는다") {
                    val dateTime = MemoDateTime.AllDay(dateRange = july(day = 14)..july(day = 16))
                    val dateRange = july(day = 16)..july(day = 18)

                    val result = useCase(parameter = MoveMemoUseCase.Parameter(id = memoId, fromDateTime = dateTime, toDateRange = dateRange))

                    result.shouldBeSuccess(Unit)
                    coVerify(exactly = 1) {
                        accountMemoRepository.updateDetail(account = account, memoId = memoId, detail = any(), updatedAt = any())
                    }
                    coVerify(exactly = 0) { accountMemoRepository.updateFinished(account = any(), memoId = any(), isFinished = any(), updatedAt = any()) }
                }
            }
        }

        Given("대상 메모가 삭제되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val memoId = fixtureMonkey.giveMeOne<Uuid>()
            val storedMemo = memo(id = memoId, isDeleted = true)
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            val findMemoUseCase = mockk<FindMemoUseCase>()
            every { findMemoUseCase(parameter = memoId) } returns flowOf(Result.success(storedMemo))
            val accountMemoRepository = mockk<AccountMemoRepository>(relaxed = true)
            val clock = mockk<Clock>(relaxed = true)
            val useCase =
                MoveMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    findMemoUseCase = findMemoUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    clock = clock,
                )

            When("새 날짜 범위로 이동한다") {
                Then("TC-CALENDAR-MEMO-MOVE-DOMAIN-004 기간 변경을 반영하지 않는다") {
                    val dateTime = MemoDateTime.AllDay(dateRange = july(day = 14)..july(day = 16))
                    val dateRange = july(day = 21)..july(day = 23)

                    val result = useCase(parameter = MoveMemoUseCase.Parameter(id = memoId, fromDateTime = dateTime, toDateRange = dateRange))

                    result.shouldBeSuccess(Unit)
                    coVerify(exactly = 0) {
                        accountMemoRepository.updateDetail(account = any(), memoId = any(), detail = any(), updatedAt = any())
                    }
                    coVerify(exactly = 0) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("대상 메모를 찾을 수 없다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val memoId = fixtureMonkey.giveMeOne<Uuid>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            val findMemoUseCase = mockk<FindMemoUseCase>()
            every { findMemoUseCase(parameter = memoId) } returns flowOf(Result.success(null))
            val accountMemoRepository = mockk<AccountMemoRepository>(relaxed = true)
            val clock = mockk<Clock>(relaxed = true)
            val useCase =
                MoveMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    findMemoUseCase = findMemoUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    clock = clock,
                )

            When("새 날짜 범위로 이동한다") {
                Then("기간 변경을 반영하지 않는다") {
                    val dateTime = MemoDateTime.AllDay(dateRange = july(day = 14)..july(day = 16))
                    val dateRange = july(day = 21)..july(day = 23)

                    val result = useCase(parameter = MoveMemoUseCase.Parameter(id = memoId, fromDateTime = dateTime, toDateRange = dateRange))

                    result.shouldBeSuccess(Unit)
                    coVerify(exactly = 0) {
                        accountMemoRepository.updateDetail(account = any(), memoId = any(), detail = any(), updatedAt = any())
                    }
                    coVerify(exactly = 0) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("메모 저장이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val memoId = fixtureMonkey.giveMeOne<Uuid>()
            val storedMemo = memo(id = memoId, isDeleted = false)
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            val findMemoUseCase = mockk<FindMemoUseCase>()
            every { findMemoUseCase(parameter = memoId) } returns flowOf(Result.success(storedMemo))
            val accountMemoRepository = mockk<AccountMemoRepository>()
            coEvery {
                accountMemoRepository.updateDetail(account = account, memoId = memoId, detail = any(), updatedAt = any())
            } throws throwable
            val clock = mockk<Clock>(relaxed = true)
            val useCase =
                MoveMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    findMemoUseCase = findMemoUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    clock = clock,
                )

            When("새 날짜 범위로 이동한다") {
                Then("TC-CALENDAR-MEMO-MOVE-DATA-002 저장 실패를 전달하고 동기화를 요청하지 않는다") {
                    val dateTime = MemoDateTime.AllDay(dateRange = july(day = 14)..july(day = 16))
                    val dateRange = july(day = 21)..july(day = 23)

                    val result = useCase(parameter = MoveMemoUseCase.Parameter(id = memoId, fromDateTime = dateTime, toDateRange = dateRange))

                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                    coVerify(exactly = 0) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun july(day: Int): LocalDate = LocalDate(year = 2026, month = Month.JULY, day = day)

        private fun memo(
            id: Uuid,
            isDeleted: Boolean,
            isFinished: Boolean = false,
        ): Memo =
            fixtureMonkey
                .giveMeKotlinBuilder<Memo>()
                .setExp(Memo::id, id)
                .setExp(Memo::isDeleted, isDeleted)
                .setExp(Memo::isFinished, isFinished)
                .setExp(Memo::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Memo::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
