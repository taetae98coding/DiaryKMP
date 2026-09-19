package io.github.taetae98coding.diary.domain.memo.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.memo.exception.MemoTitleBlankException
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoRepository
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
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AddMemoUseCaseTest :
    BehaviorSpec({
        listOf(
            "" to "빈 제목",
            "   " to "공백 문자로만 이루어진 제목",
        ).forEach { (blankTitle, label) ->
            Given("$label 이 입력되어 있다") {
                val getAccountUseCase = mockk<GetAccountUseCase>()
                val requestSyncUseCase = mockk<RequestSyncUseCase>()
                val accountMemoRepository = mockk<AccountMemoRepository>(relaxed = true)
                val clock = mockk<Clock>(relaxed = true)
                val useCase =
                    AddMemoUseCase(
                        getAccountUseCase = getAccountUseCase,
                        requestSyncUseCase = requestSyncUseCase,
                        accountMemoRepository = accountMemoRepository,
                        clock = clock,
                    )

                When("메모를 추가한다") {
                    Then("TC-MEMO-ADD-DOMAIN-001 제목 공백 예외로 실패하고 메모를 저장하지 않는다") {
                        val result = useCase(parameter = AddMemoUseCase.Parameter(detail = fixtureMonkey.giveMeOne<MemoDetail>().copy(title = blankTitle)))

                        result.shouldBeFailure().shouldBeInstanceOf<MemoTitleBlankException>()
                        verify(exactly = 0) { getAccountUseCase(parameter = Unit) }
                        coVerify(exactly = 0) { accountMemoRepository.upsert(account = any(), memo = any(), tagIdSet = any()) }
                        coVerify(exactly = 0) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                    }
                }
            }
        }

        Given("로그인한 계정과 현재 시각이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val now = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val memoSlot = slot<Memo>()
            val tagIdSetSlot = slot<Set<Uuid>>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val accountMemoRepository = mockk<AccountMemoRepository>()
            coEvery { accountMemoRepository.upsert(account = account, memo = capture(memoSlot), tagIdSet = capture(tagIdSetSlot)) } just Runs
            val clock = mockk<Clock>()
            every { clock.now() } returns now
            val useCase =
                AddMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    clock = clock,
                )

            When("공백이 아닌 제목으로 메모를 추가한다") {
                Then("TC-MEMO-ADD-DATA-001 현재 계정과 연결된 메모로 초기 상태와 함께 저장한다") {
                    val title = "title-${fixtureMonkey.giveMeOne<String>()}"

                    val result = useCase(parameter = AddMemoUseCase.Parameter(detail = fixtureMonkey.giveMeOne<MemoDetail>().copy(title = title)))

                    coVerify(exactly = 1) { accountMemoRepository.upsert(account = account, memo = any(), tagIdSet = any()) }

                    val memo = memoSlot.captured
                    result.shouldBeSuccess(memo.id)
                    memo.detail.title shouldBe title
                    memo.id shouldNotBe Uuid.NIL
                    memo.isFinished.shouldBeFalse()
                    memo.isDeleted.shouldBeFalse()
                    memo.createdAt shouldBe now
                    memo.updatedAt shouldBe now
                }

                Then("TC-MEMO-ADD-DATA-002 입력한 설명과 컬러가 저장된 메모에 포함된다") {
                    val detail = fixtureMonkey.giveMeOne<MemoDetail>().copy(title = "title-${fixtureMonkey.giveMeOne<String>()}")

                    val result = useCase(parameter = AddMemoUseCase.Parameter(detail = detail))

                    result.shouldBeSuccess()

                    val memo = memoSlot.captured
                    memo.detail.description shouldBe detail.description
                    memo.detail.color shouldBe detail.color
                }

                Then("TC-MEMO-ADD-DATA-003 선택한 기간이 종일 여부와 함께 저장된 메모에 포함된다") {
                    listOf(
                        fixtureMonkey.giveMeOne<MemoDateTime.AllDay>(),
                        fixtureMonkey.giveMeOne<MemoDateTime.DateTime>(),
                    ).forEach { dateTime ->
                        val detail =
                            fixtureMonkey.giveMeOne<MemoDetail>().copy(
                                title = "title-${fixtureMonkey.giveMeOne<String>()}",
                                dateTime = dateTime,
                            )

                        val result = useCase(parameter = AddMemoUseCase.Parameter(detail = detail))

                        result.shouldBeSuccess()
                        memoSlot.captured.detail.dateTime shouldBe dateTime
                    }
                }

                Then("TC-MEMO-ADD-DATA-004 기간을 선택하지 않으면 기간 없는 메모로 저장된다") {
                    val detail =
                        fixtureMonkey.giveMeOne<MemoDetail>().copy(
                            title = "title-${fixtureMonkey.giveMeOne<String>()}",
                            dateTime = null,
                        )

                    val result = useCase(parameter = AddMemoUseCase.Parameter(detail = detail))

                    result.shouldBeSuccess()
                    memoSlot.captured.detail.dateTime shouldBe null
                }

                Then("TC-MEMO-ADD-DATA-005 선택한 태그가 메모의 태그 연결로 함께 저장된다") {
                    val tagIdSet = setOf(fixtureMonkey.giveMeOne<Uuid>(), fixtureMonkey.giveMeOne<Uuid>())

                    val result =
                        useCase(
                            parameter =
                                AddMemoUseCase.Parameter(
                                    detail = titledDetail(),
                                    tagIdSet = tagIdSet,
                                ),
                        )

                    result.shouldBeSuccess()
                    tagIdSetSlot.captured shouldBe tagIdSet
                    memoSlot.captured.primaryTagId shouldBe null
                }

                Then("TC-MEMO-ADD-DATA-006 대표 태그로 지정한 태그가 메모의 대표 태그로 저장되고 연결에도 포함된다") {
                    val primaryTagId = fixtureMonkey.giveMeOne<Uuid>()
                    val otherTagId = fixtureMonkey.giveMeOne<Uuid>()

                    val result =
                        useCase(
                            parameter =
                                AddMemoUseCase.Parameter(
                                    detail = titledDetail(),
                                    primaryTagId = primaryTagId,
                                    tagIdSet = setOf(primaryTagId, otherTagId),
                                ),
                        )

                    result.shouldBeSuccess()
                    memoSlot.captured.primaryTagId shouldBe primaryTagId
                    tagIdSetSlot.captured shouldBe setOf(primaryTagId, otherTagId)
                }

                Then("TC-MEMO-ADD-DATA-007 태그를 선택하지 않으면 태그 연결과 대표 태그 없이 저장된다") {
                    val result = useCase(parameter = AddMemoUseCase.Parameter(detail = titledDetail()))

                    result.shouldBeSuccess()
                    memoSlot.captured.primaryTagId shouldBe null
                    tagIdSetSlot.captured.shouldBeEmpty()
                }

                Then("TC-MEMO-ADD-DATA-008 TC-MEMO-TAG-DOMAIN-005 대표 태그만 지정해도 그 태그가 태그 연결에 포함된다") {
                    val primaryTagId = fixtureMonkey.giveMeOne<Uuid>()

                    val result =
                        useCase(
                            parameter =
                                AddMemoUseCase.Parameter(
                                    detail = titledDetail(),
                                    primaryTagId = primaryTagId,
                                ),
                        )

                    result.shouldBeSuccess()
                    memoSlot.captured.primaryTagId shouldBe primaryTagId
                    tagIdSetSlot.captured shouldBe setOf(primaryTagId)
                }

                Then("TC-MEMO-ADD-DOMAIN-006 태그를 선택하지 않아도 메모 추가가 성립한다") {
                    val result = useCase(parameter = AddMemoUseCase.Parameter(detail = titledDetail()))

                    result.shouldBeSuccess(memoSlot.captured.id)
                    coVerify { accountMemoRepository.upsert(account = account, memo = any(), tagIdSet = any()) }
                }
            }
        }

        Given("로그인한 계정과 선택할 장소가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val now = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val placeIdSetSlot = slot<Set<Uuid>>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val accountMemoRepository = mockk<AccountMemoRepository>()
            coEvery { accountMemoRepository.upsert(account = account, memo = any(), tagIdSet = any(), placeIdSet = capture(placeIdSetSlot)) } just Runs
            val clock = mockk<Clock>()
            every { clock.now() } returns now
            val useCase =
                AddMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    clock = clock,
                )

            When("장소를 선택하고 메모를 추가한다") {
                Then("TC-MEMO-ADD-DATA-009 선택한 장소가 메모의 장소 연결로 함께 저장된다") {
                    val placeIdSet = setOf(fixtureMonkey.giveMeOne<Uuid>(), fixtureMonkey.giveMeOne<Uuid>())

                    val result =
                        useCase(
                            parameter =
                                AddMemoUseCase.Parameter(
                                    detail = titledDetail(),
                                    placeIdSet = placeIdSet,
                                ),
                        )

                    result.shouldBeSuccess()
                    placeIdSetSlot.captured shouldBe placeIdSet
                }
            }

            When("장소를 선택하지 않고 메모를 추가한다") {
                Then("TC-MEMO-ADD-DATA-010 장소 연결 없이 저장된다") {
                    val result = useCase(parameter = AddMemoUseCase.Parameter(detail = titledDetail()))

                    result.shouldBeSuccess()
                    placeIdSetSlot.captured.shouldBeEmpty()
                }
            }
        }

        Given("로그인한 계정과 선택할 웹 항목이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val now = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val webIdSetSlot = slot<Set<Uuid>>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val accountMemoRepository = mockk<AccountMemoRepository>()
            coEvery {
                accountMemoRepository.upsert(account = account, memo = any(), tagIdSet = any(), placeIdSet = any(), webIdSet = capture(webIdSetSlot))
            } just Runs
            val clock = mockk<Clock>()
            every { clock.now() } returns now
            val useCase =
                AddMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    clock = clock,
                )

            When("웹 항목을 선택하고 메모를 추가한다") {
                Then("TC-MEMO-ADD-DATA-015 TC-MEMO-ADD-DATA-018 선택한 웹 항목이 메모의 웹 연결로 함께 저장된다") {
                    val webIdSet = setOf(fixtureMonkey.giveMeOne<Uuid>(), fixtureMonkey.giveMeOne<Uuid>())

                    val result =
                        useCase(
                            parameter =
                                AddMemoUseCase.Parameter(
                                    detail = titledDetail(),
                                    webIdSet = webIdSet,
                                ),
                        )

                    result.shouldBeSuccess()
                    webIdSetSlot.captured shouldBe webIdSet
                }
            }

            When("웹 항목을 선택하지 않고 메모를 추가한다") {
                Then("TC-MEMO-ADD-DATA-016 웹 연결 없이 저장된다") {
                    val result = useCase(parameter = AddMemoUseCase.Parameter(detail = titledDetail()))

                    result.shouldBeSuccess()
                    webIdSetSlot.captured.shouldBeEmpty()
                }

                Then("TC-MEMO-ADD-DOMAIN-012 웹 항목을 선택하지 않아도 메모 추가가 성립한다") {
                    val result = useCase(parameter = AddMemoUseCase.Parameter(detail = titledDetail()))

                    result.shouldBeSuccess()
                    coVerify {
                        accountMemoRepository.upsert(
                            account = account,
                            memo = any(),
                            tagIdSet = any(),
                            placeIdSet = any(),
                            webIdSet = any(),
                        )
                    }
                }
            }
        }

        Given("로그인한 계정이 준비되어 있고 두 번 연속으로 메모를 추가한다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val now = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val capturedMemos = mutableListOf<Memo>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val accountMemoRepository = mockk<AccountMemoRepository>()
            coEvery { accountMemoRepository.upsert(account = account, memo = capture(capturedMemos), tagIdSet = any()) } just Runs
            val clock = mockk<Clock>()
            every { clock.now() } returns now
            val useCase =
                AddMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    clock = clock,
                )

            When("서로 다른 제목으로 메모를 두 번 추가한다") {
                Then("추가되는 메모마다 서로 다른 고유 식별자를 부여하고 반환한다") {
                    val firstId = useCase(parameter = AddMemoUseCase.Parameter(detail = fixtureMonkey.giveMeOne<MemoDetail>().copy(title = "first-${fixtureMonkey.giveMeOne<String>()}"))).shouldBeSuccess()
                    val secondId = useCase(parameter = AddMemoUseCase.Parameter(detail = fixtureMonkey.giveMeOne<MemoDetail>().copy(title = "second-${fixtureMonkey.giveMeOne<String>()}"))).shouldBeSuccess()

                    firstId shouldNotBe secondId
                    capturedMemos[0].id shouldBe firstId
                    capturedMemos[1].id shouldBe secondId
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
                AddMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    clock = clock,
                )

            When("공백이 아닌 제목으로 메모를 추가한다") {
                Then("TC-MEMO-ADD-DOMAIN-010 계정 조회 실패를 전달하고 메모를 저장하지 않는다") {
                    val result = useCase(parameter = AddMemoUseCase.Parameter(detail = fixtureMonkey.giveMeOne<MemoDetail>().copy(title = "title-${fixtureMonkey.giveMeOne<String>()}")))

                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                    coVerify(exactly = 0) { accountMemoRepository.upsert(account = any(), memo = any(), tagIdSet = any()) }
                    coVerify(exactly = 0) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("메모 저장과 동기화 요청이 성공하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val accountMemoRepository = mockk<AccountMemoRepository>()
            coEvery { accountMemoRepository.upsert(account = account, memo = any(), tagIdSet = any()) } just Runs
            val clock = mockk<Clock>()
            every { clock.now() } returns Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val useCase =
                AddMemoUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoRepository = accountMemoRepository,
                    clock = clock,
                )

            When("공백이 아닌 제목으로 메모를 추가한다") {
                Then("메모 저장 후 동기화를 한 번 요청한다") {
                    val result = useCase(parameter = AddMemoUseCase.Parameter(detail = fixtureMonkey.giveMeOne<MemoDetail>().copy(title = "title-${fixtureMonkey.giveMeOne<String>()}")))

                    result.shouldBeSuccess()
                    coVerifyOrder {
                        accountMemoRepository.upsert(account = account, memo = any(), tagIdSet = any())
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

        private fun titledDetail(): MemoDetail =
            fixtureMonkey
                .giveMeOne<MemoDetail>()
                .copy(title = "title-${fixtureMonkey.giveMeOne<String>()}")
    }
}
