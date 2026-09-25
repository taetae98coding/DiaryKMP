package io.github.taetae98coding.diary.domain.memo.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoTagRepository
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

class MemoTagUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 현재 시각이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val now = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val accountMemoTagRepository = mockk<AccountMemoTagRepository>(relaxed = true)
            val clock = mockk<Clock>()
            every { clock.now() } returns now

            When("태그를 추가한다") {
                Then("TC-SYNC-REFRESH-FEATURE-004 TC-MEMO-DETAIL-DATA-004 TC-MEMO-DETAIL-DATA-011 현재 계정의 메모에 그 태그의 연결을 만들고 동기화를 요청한다") {
                    val memoId = fixtureMonkey.giveMeOne<Uuid>()
                    val tagId = fixtureMonkey.giveMeOne<Uuid>()
                    val useCase =
                        AddMemoTagUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountMemoTagRepository = accountMemoTagRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = AddMemoTagUseCase.Parameter(memoId = memoId, tagId = tagId))

                    result.shouldBeSuccess()
                    coVerifyOrder {
                        accountMemoTagRepository.upsert(account = account, memoId = memoId, tagId = tagId, isDeleted = false, updatedAt = now)
                        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                    }
                }
            }

            When("태그를 제거한다") {
                Then("TC-SYNC-REFRESH-FEATURE-004 TC-MEMO-DETAIL-DATA-005 TC-MEMO-DETAIL-DATA-011 현재 계정의 메모에서 그 태그의 연결을 해제하고 동기화를 요청한다") {
                    val memoId = fixtureMonkey.giveMeOne<Uuid>()
                    val tagId = fixtureMonkey.giveMeOne<Uuid>()
                    val useCase =
                        RemoveMemoTagUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountMemoTagRepository = accountMemoTagRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = RemoveMemoTagUseCase.Parameter(memoId = memoId, tagId = tagId))

                    result.shouldBeSuccess()
                    coVerifyOrder {
                        accountMemoTagRepository.upsert(account = account, memoId = memoId, tagId = tagId, isDeleted = true, updatedAt = now)
                        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                    }
                }
            }

            When("대표 태그를 지정한다") {
                Then("TC-SYNC-REFRESH-FEATURE-004 TC-MEMO-DETAIL-DATA-008 TC-MEMO-DETAIL-DATA-011 현재 계정의 메모에 그 태그를 대표 태그로 지정하고 동기화를 요청한다") {
                    val memoId = fixtureMonkey.giveMeOne<Uuid>()
                    val tagId = fixtureMonkey.giveMeOne<Uuid>()
                    val useCase =
                        SetMemoPrimaryTagUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountMemoTagRepository = accountMemoTagRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = SetMemoPrimaryTagUseCase.Parameter(memoId = memoId, tagId = tagId))

                    result.shouldBeSuccess()
                    coVerifyOrder {
                        accountMemoTagRepository.updatePrimaryTagId(account = account, memoId = memoId, primaryTagId = tagId, updatedAt = now)
                        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                    }
                }
            }

            When("대표 태그 지정을 해제한다") {
                Then("TC-SYNC-REFRESH-FEATURE-004 TC-MEMO-DETAIL-DATA-009 TC-MEMO-DETAIL-DATA-011 현재 계정의 메모에서 대표 태그를 비우고 동기화를 요청한다") {
                    val memoId = fixtureMonkey.giveMeOne<Uuid>()
                    val useCase =
                        UnsetMemoPrimaryTagUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountMemoTagRepository = accountMemoTagRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = memoId)

                    result.shouldBeSuccess()
                    coVerifyOrder {
                        accountMemoTagRepository.updatePrimaryTagId(account = account, memoId = memoId, primaryTagId = null, updatedAt = now)
                        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                    }
                }
            }
        }

        Given("동기화 요청이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val now = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.failure(throwable)
            val accountMemoTagRepository = mockk<AccountMemoTagRepository>(relaxed = true)
            val clock = mockk<Clock>()
            every { clock.now() } returns now

            When("태그를 추가한다") {
                Then("TC-MEMO-DETAIL-DATA-012 저장한 태그 연결을 되돌리지 않는다") {
                    val memoId = fixtureMonkey.giveMeOne<Uuid>()
                    val tagId = fixtureMonkey.giveMeOne<Uuid>()
                    val useCase =
                        AddMemoTagUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountMemoTagRepository = accountMemoTagRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = AddMemoTagUseCase.Parameter(memoId = memoId, tagId = tagId))

                    result.shouldBeSuccess()
                    coVerify(exactly = 1) {
                        accountMemoTagRepository.upsert(account = account, memoId = memoId, tagId = tagId, isDeleted = false, updatedAt = now)
                    }
                    coVerify(exactly = 0) {
                        accountMemoTagRepository.upsert(account = account, memoId = memoId, tagId = tagId, isDeleted = true, updatedAt = any())
                    }
                }
            }

            When("대표 태그를 지정한다") {
                Then("TC-MEMO-DETAIL-DATA-012 저장한 대표 태그를 되돌리지 않는다") {
                    val memoId = fixtureMonkey.giveMeOne<Uuid>()
                    val tagId = fixtureMonkey.giveMeOne<Uuid>()
                    val useCase =
                        SetMemoPrimaryTagUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountMemoTagRepository = accountMemoTagRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = SetMemoPrimaryTagUseCase.Parameter(memoId = memoId, tagId = tagId))

                    result.shouldBeSuccess()
                    coVerify(exactly = 1) {
                        accountMemoTagRepository.updatePrimaryTagId(account = account, memoId = memoId, primaryTagId = tagId, updatedAt = now)
                    }
                    coVerify(exactly = 0) {
                        accountMemoTagRepository.updatePrimaryTagId(account = account, memoId = memoId, primaryTagId = null, updatedAt = any())
                    }
                }
            }
        }

        Given("저장이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            val accountMemoTagRepository = mockk<AccountMemoTagRepository>()
            coEvery {
                accountMemoTagRepository.upsert(
                    account = any(),
                    memoId = any(),
                    tagId = any(),
                    isDeleted = any(),
                    updatedAt = any(),
                )
            } throws throwable
            val clock = mockk<Clock>(relaxed = true)
            val useCase =
                AddMemoTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoTagRepository = accountMemoTagRepository,
                    clock = clock,
                )

            When("태그를 추가한다") {
                Then("TC-MEMO-DETAIL-FEATURE-037 실패를 그대로 전달하고 동기화를 요청하지 않는다") {
                    val result =
                        useCase(
                            parameter =
                                AddMemoTagUseCase.Parameter(
                                    memoId = fixtureMonkey.giveMeOne<Uuid>(),
                                    tagId = fixtureMonkey.giveMeOne<Uuid>(),
                                ),
                        )

                    result.shouldBeFailure().shouldBeSameInstanceAs(throwable)
                    coVerify(exactly = 0) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            val accountMemoTagRepository = mockk<AccountMemoTagRepository>(relaxed = true)
            val clock = mockk<Clock>(relaxed = true)
            val useCase =
                UnsetMemoPrimaryTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoTagRepository = accountMemoTagRepository,
                    clock = clock,
                )

            When("대표 태그 지정을 해제한다") {
                Then("TC-MEMO-DETAIL-FEATURE-037 계정 조회 실패를 그대로 전달하고 저장과 동기화를 시도하지 않는다") {
                    val result = useCase(parameter = fixtureMonkey.giveMeOne<Uuid>())

                    result.shouldBeFailure().shouldBeSameInstanceAs(throwable)
                    coVerify(exactly = 0) {
                        accountMemoTagRepository.updatePrimaryTagId(
                            account = any(),
                            memoId = any(),
                            primaryTagId = any(),
                            updatedAt = any(),
                        )
                    }
                    coVerify(exactly = 0) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
