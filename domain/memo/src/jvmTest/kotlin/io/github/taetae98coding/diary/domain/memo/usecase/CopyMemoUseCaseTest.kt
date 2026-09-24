package io.github.taetae98coding.diary.domain.memo.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoContactRepository
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoPlaceRepository
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoRepository
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoTagRepository
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoWebRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class CopyMemoUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정의 메모와 복사 시점이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val now = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val source = memo()
            val memoSlot = slot<Memo>()
            val tagIdSetSlot = slot<Set<Uuid>>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val findMemoUseCase = mockk<FindMemoUseCase>()
            every { findMemoUseCase(parameter = source.id) } returns flowOf(Result.success(source))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val accountMemoRepository = mockk<AccountMemoRepository>()
            coEvery { accountMemoRepository.upsert(account = account, memo = capture(memoSlot), tagIdSet = capture(tagIdSetSlot)) } just Runs
            val accountMemoTagRepository = mockk<AccountMemoTagRepository>()
            val accountMemoWebRepository = mockk<AccountMemoWebRepository>()
            val accountMemoContactRepository = mockk<AccountMemoContactRepository>()
            coEvery { accountMemoContactRepository.findContactIdSet(account = account, memoId = source.id) } returns emptySet()
            val accountMemoPlaceRepository = mockk<AccountMemoPlaceRepository>()
            coEvery { accountMemoPlaceRepository.findPlaceIdSet(account = account, memoId = source.id) } returns emptySet()
            coEvery { accountMemoWebRepository.findWebIdSet(account = account, memoId = source.id) } returns emptySet()
            coEvery { accountMemoTagRepository.findTagIdSet(account = account, memoId = source.id) } returns emptySet()
            val clock = mockk<Clock>()
            every { clock.now() } returns now
            val useCase =
                CopyMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    findMemoUseCase = findMemoUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    accountMemoTagRepository = accountMemoTagRepository,
                    accountMemoWebRepository = accountMemoWebRepository,
                    accountMemoContactRepository = accountMemoContactRepository,
                    accountMemoPlaceRepository = accountMemoPlaceRepository,
                    clock = clock,
                )

            When("메모를 복사한다") {
                Then("TC-MEMO-DETAIL-DATA-013 원본의 내용을 가진 새 메모를 현재 계정과 연결해 저장하고 복사본 식별자를 반환한다") {
                    val result = useCase(parameter = source.id)

                    coVerify(exactly = 1) { accountMemoRepository.upsert(account = account, memo = any(), tagIdSet = any()) }

                    val memo = memoSlot.captured
                    result.shouldBeSuccess(memo.id)
                    memo.id shouldNotBe source.id
                    memo.id shouldNotBe Uuid.NIL
                    memo.detail shouldBe source.detail
                    memo.createdAt shouldBe now
                    memo.updatedAt shouldBe now
                }

                Then("TC-MEMO-DETAIL-DATA-016 원본 메모와 원본의 태그 연결을 바꾸지 않는다") {
                    val result = useCase(parameter = source.id)

                    result.shouldBeSuccess()
                    memoSlot.captured.id shouldNotBe source.id
                    coVerify(exactly = 0) { accountMemoRepository.updateDetail(account = any(), memoId = any(), detail = any(), updatedAt = any()) }
                    coVerify(exactly = 0) { accountMemoRepository.updateFinished(account = any(), memoId = any(), isFinished = any(), updatedAt = any()) }
                    coVerify(exactly = 0) { accountMemoRepository.updateDeleted(account = any(), memoId = any(), isDeleted = any(), updatedAt = any()) }
                    coVerify(exactly = 0) { accountMemoTagRepository.upsert(account = any(), memoId = any(), tagId = any(), isDeleted = any(), updatedAt = any()) }
                    coVerify(exactly = 0) { accountMemoTagRepository.updatePrimaryTagId(account = any(), memoId = any(), primaryTagId = any(), updatedAt = any()) }
                }
            }
        }

        Given("복사본 저장과 동기화 요청이 성공하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val source = memo()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val findMemoUseCase = mockk<FindMemoUseCase>()
            every { findMemoUseCase(parameter = source.id) } returns flowOf(Result.success(source))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val accountMemoRepository = mockk<AccountMemoRepository>()
            coEvery { accountMemoRepository.upsert(account = account, memo = any(), tagIdSet = any()) } just Runs
            val accountMemoTagRepository = mockk<AccountMemoTagRepository>()
            val accountMemoWebRepository = mockk<AccountMemoWebRepository>()
            val accountMemoContactRepository = mockk<AccountMemoContactRepository>()
            coEvery { accountMemoContactRepository.findContactIdSet(account = account, memoId = source.id) } returns emptySet()
            val accountMemoPlaceRepository = mockk<AccountMemoPlaceRepository>()
            coEvery { accountMemoPlaceRepository.findPlaceIdSet(account = account, memoId = source.id) } returns emptySet()
            coEvery { accountMemoWebRepository.findWebIdSet(account = account, memoId = source.id) } returns emptySet()
            coEvery { accountMemoTagRepository.findTagIdSet(account = account, memoId = source.id) } returns emptySet()
            val clock = mockk<Clock>()
            every { clock.now() } returns Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val useCase =
                CopyMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    findMemoUseCase = findMemoUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    accountMemoTagRepository = accountMemoTagRepository,
                    accountMemoWebRepository = accountMemoWebRepository,
                    accountMemoContactRepository = accountMemoContactRepository,
                    accountMemoPlaceRepository = accountMemoPlaceRepository,
                    clock = clock,
                )

            When("메모를 복사한다") {
                Then("TC-MEMO-DETAIL-DATA-017 복사본을 저장한 뒤 동기화를 한 번 요청한다") {
                    val result = useCase(parameter = source.id)

                    result.shouldBeSuccess()
                    coVerifyOrder {
                        accountMemoRepository.upsert(account = account, memo = any(), tagIdSet = any())
                        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                    }
                    coVerify(exactly = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        listOf(
            "완료되지 않고 삭제되지 않은" to memo().copy(isFinished = false, isDeleted = false),
            "완료된" to memo().copy(isFinished = true, isDeleted = false),
            "삭제된" to memo().copy(isFinished = false, isDeleted = true),
        ).forEach { (label, source) ->
            Given("$label 메모가 준비되어 있다") {
                val account = fixtureMonkey.giveMeOne<Account.User>()
                val memoSlot = slot<Memo>()
                val getAccountUseCase = mockk<GetAccountUseCase>()
                every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
                val findMemoUseCase = mockk<FindMemoUseCase>()
                every { findMemoUseCase(parameter = source.id) } returns flowOf(Result.success(source))
                val requestSyncUseCase = mockk<RequestSyncUseCase>()
                coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
                val accountMemoRepository = mockk<AccountMemoRepository>()
                coEvery { accountMemoRepository.upsert(account = account, memo = capture(memoSlot), tagIdSet = any()) } just Runs
                val accountMemoTagRepository = mockk<AccountMemoTagRepository>()
                val accountMemoWebRepository = mockk<AccountMemoWebRepository>()
                val accountMemoContactRepository = mockk<AccountMemoContactRepository>()
                coEvery { accountMemoContactRepository.findContactIdSet(account = account, memoId = source.id) } returns emptySet()
                val accountMemoPlaceRepository = mockk<AccountMemoPlaceRepository>()
                coEvery { accountMemoPlaceRepository.findPlaceIdSet(account = account, memoId = source.id) } returns emptySet()
                coEvery { accountMemoWebRepository.findWebIdSet(account = account, memoId = source.id) } returns emptySet()
                coEvery { accountMemoTagRepository.findTagIdSet(account = account, memoId = source.id) } returns emptySet()
                val clock = mockk<Clock>()
                every { clock.now() } returns Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
                val useCase =
                    CopyMemoUseCase(
                        getAccountUseCase = getAccountUseCase,
                        findMemoUseCase = findMemoUseCase,
                        requestSyncUseCase = requestSyncUseCase,
                        accountMemoRepository = accountMemoRepository,
                        accountMemoTagRepository = accountMemoTagRepository,
                        accountMemoWebRepository = accountMemoWebRepository,
                        accountMemoContactRepository = accountMemoContactRepository,
                        accountMemoPlaceRepository = accountMemoPlaceRepository,
                        clock = clock,
                    )

                When("메모를 복사한다") {
                    Then("TC-MEMO-DETAIL-DATA-014 복사본은 완료되지 않고 삭제되지 않은 상태로 저장된다") {
                        val result = useCase(parameter = source.id)

                        result.shouldBeSuccess()
                        memoSlot.captured.isFinished.shouldBeFalse()
                        memoSlot.captured.isDeleted.shouldBeFalse()
                    }
                }
            }
        }

        Given("연결된 태그와 대표 태그를 가진 메모가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val primaryTagId = fixtureMonkey.giveMeOne<Uuid>()
            val otherTagId = fixtureMonkey.giveMeOne<Uuid>()
            val source = memo().copy(primaryTagId = primaryTagId)
            val memoSlot = slot<Memo>()
            val tagIdSetSlot = slot<Set<Uuid>>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val findMemoUseCase = mockk<FindMemoUseCase>()
            every { findMemoUseCase(parameter = source.id) } returns flowOf(Result.success(source))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val accountMemoRepository = mockk<AccountMemoRepository>()
            coEvery { accountMemoRepository.upsert(account = account, memo = capture(memoSlot), tagIdSet = capture(tagIdSetSlot)) } just Runs
            val accountMemoTagRepository = mockk<AccountMemoTagRepository>()
            val accountMemoWebRepository = mockk<AccountMemoWebRepository>()
            val accountMemoContactRepository = mockk<AccountMemoContactRepository>()
            coEvery { accountMemoContactRepository.findContactIdSet(account = account, memoId = source.id) } returns emptySet()
            val accountMemoPlaceRepository = mockk<AccountMemoPlaceRepository>()
            coEvery { accountMemoPlaceRepository.findPlaceIdSet(account = account, memoId = source.id) } returns emptySet()
            coEvery { accountMemoWebRepository.findWebIdSet(account = account, memoId = source.id) } returns emptySet()
            coEvery { accountMemoTagRepository.findTagIdSet(account = account, memoId = source.id) } returns setOf(primaryTagId, otherTagId)
            val clock = mockk<Clock>()
            every { clock.now() } returns Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val useCase =
                CopyMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    findMemoUseCase = findMemoUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    accountMemoTagRepository = accountMemoTagRepository,
                    accountMemoWebRepository = accountMemoWebRepository,
                    accountMemoContactRepository = accountMemoContactRepository,
                    accountMemoPlaceRepository = accountMemoPlaceRepository,
                    clock = clock,
                )

            When("메모를 복사한다") {
                Then("TC-MEMO-DETAIL-DATA-015 원본의 연결된 태그와 대표 태그가 복사본에 저장된다") {
                    val result = useCase(parameter = source.id)

                    result.shouldBeSuccess()
                    tagIdSetSlot.captured shouldBe setOf(primaryTagId, otherTagId)
                    memoSlot.captured.primaryTagId shouldBe primaryTagId
                }
            }
        }

        Given("연결된 웹 항목을 가진 메모가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val firstWebId = fixtureMonkey.giveMeOne<Uuid>()
            val secondWebId = fixtureMonkey.giveMeOne<Uuid>()
            val source = memo()
            val webIdSetSlot = slot<Set<Uuid>>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val findMemoUseCase = mockk<FindMemoUseCase>()
            every { findMemoUseCase(parameter = source.id) } returns flowOf(Result.success(source))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val accountMemoRepository = mockk<AccountMemoRepository>()
            coEvery {
                accountMemoRepository.upsert(account = account, memo = any(), tagIdSet = any(), webIdSet = capture(webIdSetSlot))
            } just Runs
            val accountMemoTagRepository = mockk<AccountMemoTagRepository>()
            coEvery { accountMemoTagRepository.findTagIdSet(account = account, memoId = source.id) } returns emptySet()
            val accountMemoWebRepository = mockk<AccountMemoWebRepository>()
            val accountMemoContactRepository = mockk<AccountMemoContactRepository>()
            coEvery { accountMemoContactRepository.findContactIdSet(account = account, memoId = source.id) } returns emptySet()
            val accountMemoPlaceRepository = mockk<AccountMemoPlaceRepository>()
            coEvery { accountMemoPlaceRepository.findPlaceIdSet(account = account, memoId = source.id) } returns emptySet()
            coEvery { accountMemoWebRepository.findWebIdSet(account = account, memoId = source.id) } returns setOf(firstWebId, secondWebId)
            val clock = mockk<Clock>()
            every { clock.now() } returns Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val useCase =
                CopyMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    findMemoUseCase = findMemoUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    accountMemoTagRepository = accountMemoTagRepository,
                    accountMemoWebRepository = accountMemoWebRepository,
                    accountMemoContactRepository = accountMemoContactRepository,
                    accountMemoPlaceRepository = accountMemoPlaceRepository,
                    clock = clock,
                )

            When("메모를 복사한다") {
                Then("TC-MEMO-DETAIL-DATA-028 원본의 연결된 웹 항목이 복사본에 저장된다") {
                    val result = useCase(parameter = source.id)

                    result.shouldBeSuccess()
                    webIdSetSlot.captured shouldBe setOf(firstWebId, secondWebId)
                }

                Then("TC-MEMO-DETAIL-DATA-029 원본의 웹 연결은 바뀌지 않는다") {
                    useCase(parameter = source.id).shouldBeSuccess()

                    coVerify(exactly = 0) {
                        accountMemoWebRepository.upsert(
                            account = any(),
                            memoId = any(),
                            webId = any(),
                            isDeleted = any(),
                            updatedAt = any(),
                        )
                    }
                }
            }
        }

        Given("연결된 태그가 없고 대표 태그만 남은 메모가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val primaryTagId = fixtureMonkey.giveMeOne<Uuid>()
            val source = memo().copy(primaryTagId = primaryTagId)
            val tagIdSetSlot = slot<Set<Uuid>>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val findMemoUseCase = mockk<FindMemoUseCase>()
            every { findMemoUseCase(parameter = source.id) } returns flowOf(Result.success(source))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val accountMemoRepository = mockk<AccountMemoRepository>()
            coEvery { accountMemoRepository.upsert(account = account, memo = any(), tagIdSet = capture(tagIdSetSlot)) } just Runs
            val accountMemoTagRepository = mockk<AccountMemoTagRepository>()
            val accountMemoWebRepository = mockk<AccountMemoWebRepository>()
            val accountMemoContactRepository = mockk<AccountMemoContactRepository>()
            coEvery { accountMemoContactRepository.findContactIdSet(account = account, memoId = source.id) } returns emptySet()
            val accountMemoPlaceRepository = mockk<AccountMemoPlaceRepository>()
            coEvery { accountMemoPlaceRepository.findPlaceIdSet(account = account, memoId = source.id) } returns emptySet()
            coEvery { accountMemoWebRepository.findWebIdSet(account = account, memoId = source.id) } returns emptySet()
            coEvery { accountMemoTagRepository.findTagIdSet(account = account, memoId = source.id) } returns emptySet()
            val clock = mockk<Clock>()
            every { clock.now() } returns Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val useCase =
                CopyMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    findMemoUseCase = findMemoUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    accountMemoTagRepository = accountMemoTagRepository,
                    accountMemoWebRepository = accountMemoWebRepository,
                    accountMemoContactRepository = accountMemoContactRepository,
                    accountMemoPlaceRepository = accountMemoPlaceRepository,
                    clock = clock,
                )

            When("메모를 복사한다") {
                Then("대표 태그가 복사본의 태그 연결에도 포함된다") {
                    val result = useCase(parameter = source.id)

                    result.shouldBeSuccess()
                    tagIdSetSlot.captured shouldBe setOf(primaryTagId)
                }
            }
        }

        Given("장소가 연결된 메모가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val firstPlaceId = fixtureMonkey.giveMeOne<Uuid>()
            val secondPlaceId = fixtureMonkey.giveMeOne<Uuid>()
            val source = memo()
            val placeIdSetSlot = slot<Set<Uuid>>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val findMemoUseCase = mockk<FindMemoUseCase>()
            every { findMemoUseCase(parameter = source.id) } returns flowOf(Result.success(source))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val accountMemoRepository = mockk<AccountMemoRepository>()
            coEvery { accountMemoRepository.upsert(account = account, memo = any(), tagIdSet = any(), placeIdSet = capture(placeIdSetSlot)) } just Runs
            val accountMemoTagRepository = mockk<AccountMemoTagRepository>()
            val accountMemoWebRepository = mockk<AccountMemoWebRepository>()
            val accountMemoContactRepository = mockk<AccountMemoContactRepository>()
            coEvery { accountMemoContactRepository.findContactIdSet(account = account, memoId = source.id) } returns emptySet()
            val accountMemoPlaceRepository = mockk<AccountMemoPlaceRepository>()
            coEvery { accountMemoPlaceRepository.findPlaceIdSet(account = account, memoId = source.id) } returns setOf(firstPlaceId, secondPlaceId)
            coEvery { accountMemoWebRepository.findWebIdSet(account = account, memoId = source.id) } returns emptySet()
            coEvery { accountMemoTagRepository.findTagIdSet(account = account, memoId = source.id) } returns emptySet()
            val clock = mockk<Clock>()
            every { clock.now() } returns Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val useCase =
                CopyMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    findMemoUseCase = findMemoUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    accountMemoTagRepository = accountMemoTagRepository,
                    accountMemoWebRepository = accountMemoWebRepository,
                    accountMemoContactRepository = accountMemoContactRepository,
                    accountMemoPlaceRepository = accountMemoPlaceRepository,
                    clock = clock,
                )

            When("메모를 복사한다") {
                Then("TC-MEMO-DETAIL-DATA-039 원본의 연결된 장소가 복사본에 저장된다") {
                    val result = useCase(parameter = source.id)

                    result.shouldBeSuccess()
                    placeIdSetSlot.captured shouldBe setOf(firstPlaceId, secondPlaceId)
                }

                Then("TC-MEMO-DETAIL-DATA-041 원본의 장소 연결은 바뀌지 않는다") {
                    useCase(parameter = source.id).shouldBeSuccess()

                    coVerify(exactly = 0) {
                        accountMemoPlaceRepository.upsert(
                            account = any(),
                            memoId = any(),
                            placeId = any(),
                            isDeleted = any(),
                            updatedAt = any(),
                        )
                    }
                }
            }
        }

        Given("대표 태그가 없고 연결된 태그도 없는 메모가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val source = memo().copy(primaryTagId = null)
            val memoSlot = slot<Memo>()
            val tagIdSetSlot = slot<Set<Uuid>>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val findMemoUseCase = mockk<FindMemoUseCase>()
            every { findMemoUseCase(parameter = source.id) } returns flowOf(Result.success(source))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val accountMemoRepository = mockk<AccountMemoRepository>()
            coEvery { accountMemoRepository.upsert(account = account, memo = capture(memoSlot), tagIdSet = capture(tagIdSetSlot)) } just Runs
            val accountMemoTagRepository = mockk<AccountMemoTagRepository>()
            val accountMemoWebRepository = mockk<AccountMemoWebRepository>()
            val accountMemoContactRepository = mockk<AccountMemoContactRepository>()
            coEvery { accountMemoContactRepository.findContactIdSet(account = account, memoId = source.id) } returns emptySet()
            val accountMemoPlaceRepository = mockk<AccountMemoPlaceRepository>()
            coEvery { accountMemoPlaceRepository.findPlaceIdSet(account = account, memoId = source.id) } returns emptySet()
            coEvery { accountMemoWebRepository.findWebIdSet(account = account, memoId = source.id) } returns emptySet()
            coEvery { accountMemoTagRepository.findTagIdSet(account = account, memoId = source.id) } returns emptySet()
            val clock = mockk<Clock>()
            every { clock.now() } returns Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val useCase =
                CopyMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    findMemoUseCase = findMemoUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    accountMemoTagRepository = accountMemoTagRepository,
                    accountMemoWebRepository = accountMemoWebRepository,
                    accountMemoContactRepository = accountMemoContactRepository,
                    accountMemoPlaceRepository = accountMemoPlaceRepository,
                    clock = clock,
                )

            When("메모를 복사한다") {
                Then("복사본도 태그 연결과 대표 태그 없이 저장된다") {
                    val result = useCase(parameter = source.id)

                    result.shouldBeSuccess()
                    tagIdSetSlot.captured.shouldBeEmpty()
                    memoSlot.captured.primaryTagId shouldBe null
                }
            }
        }

        Given("복사할 메모를 조회할 수 없도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val memoId = fixtureMonkey.giveMeOne<Uuid>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val findMemoUseCase = mockk<FindMemoUseCase>()
            every { findMemoUseCase(parameter = memoId) } returns flowOf(Result.success(null))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            val accountMemoRepository = mockk<AccountMemoRepository>(relaxed = true)
            val accountMemoTagRepository = mockk<AccountMemoTagRepository>(relaxed = true)
            val accountMemoWebRepository = mockk<AccountMemoWebRepository>(relaxed = true)
            val accountMemoContactRepository = mockk<AccountMemoContactRepository>(relaxed = true)
            val accountMemoPlaceRepository = mockk<AccountMemoPlaceRepository>(relaxed = true)
            val clock = mockk<Clock>(relaxed = true)
            val useCase =
                CopyMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    findMemoUseCase = findMemoUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    accountMemoTagRepository = accountMemoTagRepository,
                    accountMemoWebRepository = accountMemoWebRepository,
                    accountMemoContactRepository = accountMemoContactRepository,
                    accountMemoPlaceRepository = accountMemoPlaceRepository,
                    clock = clock,
                )

            When("메모를 복사한다") {
                Then("실패를 전달하고 아무것도 저장하지 않는다") {
                    val result = useCase(parameter = memoId)

                    result.shouldBeFailure().shouldBeInstanceOf<IllegalArgumentException>()
                    coVerify(exactly = 0) { accountMemoRepository.upsert(account = any(), memo = any(), tagIdSet = any()) }
                    coVerify(exactly = 0) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("메모 조회가 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val memoId = fixtureMonkey.giveMeOne<Uuid>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val findMemoUseCase = mockk<FindMemoUseCase>()
            every { findMemoUseCase(parameter = memoId) } returns flowOf(Result.failure(throwable))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            val accountMemoRepository = mockk<AccountMemoRepository>(relaxed = true)
            val accountMemoTagRepository = mockk<AccountMemoTagRepository>(relaxed = true)
            val accountMemoWebRepository = mockk<AccountMemoWebRepository>(relaxed = true)
            val accountMemoContactRepository = mockk<AccountMemoContactRepository>(relaxed = true)
            val accountMemoPlaceRepository = mockk<AccountMemoPlaceRepository>(relaxed = true)
            val clock = mockk<Clock>(relaxed = true)
            val useCase =
                CopyMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    findMemoUseCase = findMemoUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    accountMemoTagRepository = accountMemoTagRepository,
                    accountMemoWebRepository = accountMemoWebRepository,
                    accountMemoContactRepository = accountMemoContactRepository,
                    accountMemoPlaceRepository = accountMemoPlaceRepository,
                    clock = clock,
                )

            When("메모를 복사한다") {
                Then("조회 실패를 그대로 전달하고 아무것도 저장하지 않는다") {
                    val result = useCase(parameter = memoId)

                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                    coVerify(exactly = 0) { accountMemoRepository.upsert(account = any(), memo = any(), tagIdSet = any()) }
                    coVerify(exactly = 0) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val memoId = fixtureMonkey.giveMeOne<Uuid>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val findMemoUseCase = mockk<FindMemoUseCase>(relaxed = true)
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            val accountMemoRepository = mockk<AccountMemoRepository>(relaxed = true)
            val accountMemoTagRepository = mockk<AccountMemoTagRepository>(relaxed = true)
            val accountMemoWebRepository = mockk<AccountMemoWebRepository>(relaxed = true)
            val accountMemoContactRepository = mockk<AccountMemoContactRepository>(relaxed = true)
            val accountMemoPlaceRepository = mockk<AccountMemoPlaceRepository>(relaxed = true)
            val clock = mockk<Clock>(relaxed = true)
            val useCase =
                CopyMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    findMemoUseCase = findMemoUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    accountMemoTagRepository = accountMemoTagRepository,
                    accountMemoWebRepository = accountMemoWebRepository,
                    accountMemoContactRepository = accountMemoContactRepository,
                    accountMemoPlaceRepository = accountMemoPlaceRepository,
                    clock = clock,
                )

            When("메모를 복사한다") {
                Then("계정 조회 실패를 전달하고 아무것도 저장하지 않는다") {
                    val result = useCase(parameter = memoId)

                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                    coVerify(exactly = 0) { accountMemoRepository.upsert(account = any(), memo = any(), tagIdSet = any()) }
                    coVerify(exactly = 0) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("같은 메모를 두 번 연속으로 복사한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val source = memo()
            val capturedMemoList = mutableListOf<Memo>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val findMemoUseCase = mockk<FindMemoUseCase>()
            every { findMemoUseCase(parameter = source.id) } returns flowOf(Result.success(source))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val accountMemoRepository = mockk<AccountMemoRepository>()
            coEvery { accountMemoRepository.upsert(account = account, memo = capture(capturedMemoList), tagIdSet = any()) } just Runs
            val accountMemoTagRepository = mockk<AccountMemoTagRepository>()
            val accountMemoWebRepository = mockk<AccountMemoWebRepository>()
            val accountMemoContactRepository = mockk<AccountMemoContactRepository>()
            coEvery { accountMemoContactRepository.findContactIdSet(account = account, memoId = source.id) } returns emptySet()
            val accountMemoPlaceRepository = mockk<AccountMemoPlaceRepository>()
            coEvery { accountMemoPlaceRepository.findPlaceIdSet(account = account, memoId = source.id) } returns emptySet()
            coEvery { accountMemoWebRepository.findWebIdSet(account = account, memoId = source.id) } returns emptySet()
            coEvery { accountMemoTagRepository.findTagIdSet(account = account, memoId = source.id) } returns emptySet()
            val clock = mockk<Clock>()
            every { clock.now() } returns Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val useCase =
                CopyMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    findMemoUseCase = findMemoUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    accountMemoTagRepository = accountMemoTagRepository,
                    accountMemoWebRepository = accountMemoWebRepository,
                    accountMemoContactRepository = accountMemoContactRepository,
                    accountMemoPlaceRepository = accountMemoPlaceRepository,
                    clock = clock,
                )

            When("복사를 두 번 실행한다") {
                Then("복사본마다 서로 다른 고유 식별자를 부여하고 반환한다") {
                    val firstId = useCase(parameter = source.id).shouldBeSuccess()
                    val secondId = useCase(parameter = source.id).shouldBeSuccess()

                    firstId shouldNotBe secondId
                    capturedMemoList[0].id shouldBe firstId
                    capturedMemoList[1].id shouldBe secondId
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun memo(): Memo =
            fixtureMonkey
                .giveMeKotlinBuilder<Memo>()
                .setExp(Memo::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Memo::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
