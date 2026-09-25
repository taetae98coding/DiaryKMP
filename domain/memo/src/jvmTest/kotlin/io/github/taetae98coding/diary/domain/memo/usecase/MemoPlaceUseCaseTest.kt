package io.github.taetae98coding.diary.domain.memo.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.memo.repository.AccountMemoPlaceRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class MemoPlaceUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 현재 시각이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val now = fixtureMonkey.giveMeOne<Instant>()
            val getAccountUseCase = accountUseCase(account = account)
            val accountMemoPlaceRepository = mockk<AccountMemoPlaceRepository>(relaxed = true)
            val requestSyncUseCase = mockk<RequestSyncUseCase>(relaxed = true)
            val clock = mockk<Clock>()
            every { clock.now() } returns now

            When("장소를 추가한다") {
                Then("TC-MEMO-DETAIL-DATA-020 메모와 그 장소의 연결을 해제되지 않은 상태와 선택 시점으로 저장한다") {
                    val memoId = fixtureMonkey.giveMeOne<Uuid>()
                    val placeId = fixtureMonkey.giveMeOne<Uuid>()
                    val useCase =
                        AddMemoPlaceUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountMemoPlaceRepository = accountMemoPlaceRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = AddMemoPlaceUseCase.Parameter(memoId = memoId, placeId = placeId))

                    result.shouldBeSuccess()
                    coVerify(exactly = 1) {
                        accountMemoPlaceRepository.upsert(
                            account = account,
                            memoId = memoId,
                            placeId = placeId,
                            isDeleted = false,
                            updatedAt = now,
                        )
                    }
                }

                Then("TC-MEMO-DETAIL-DATA-042 TC-MEMO-PLACE-DATA-005 장소 선택은 연결 변경을 서버와 맞추기 위한 동기화를 요청한다") {
                    val requestSyncUseCase = mockk<RequestSyncUseCase>(relaxed = true)
                    val useCase =
                        AddMemoPlaceUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountMemoPlaceRepository = accountMemoPlaceRepository,
                            clock = clock,
                        )

                    val result =
                        useCase(
                            parameter =
                                AddMemoPlaceUseCase.Parameter(
                                    memoId = fixtureMonkey.giveMeOne<Uuid>(),
                                    placeId = fixtureMonkey.giveMeOne<Uuid>(),
                                ),
                        )

                    result.shouldBeSuccess()
                    coVerify(exactly = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }

            When("장소를 해제한다") {
                Then("TC-MEMO-DETAIL-DATA-042 장소 선택 해제는 연결 변경을 서버와 맞추기 위한 동기화를 요청한다") {
                    val requestSyncUseCase = mockk<RequestSyncUseCase>(relaxed = true)
                    val useCase =
                        RemoveMemoPlaceUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountMemoPlaceRepository = accountMemoPlaceRepository,
                            clock = clock,
                        )

                    val result =
                        useCase(
                            parameter =
                                RemoveMemoPlaceUseCase.Parameter(
                                    memoId = fixtureMonkey.giveMeOne<Uuid>(),
                                    placeId = fixtureMonkey.giveMeOne<Uuid>(),
                                ),
                        )

                    result.shouldBeSuccess()
                    coVerify(exactly = 1) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }

                Then("TC-MEMO-DETAIL-DATA-021 메모와 그 장소의 연결을 해제 상태와 해제 시점으로 저장한다") {
                    val memoId = fixtureMonkey.giveMeOne<Uuid>()
                    val placeId = fixtureMonkey.giveMeOne<Uuid>()
                    val useCase =
                        RemoveMemoPlaceUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountMemoPlaceRepository = accountMemoPlaceRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = RemoveMemoPlaceUseCase.Parameter(memoId = memoId, placeId = placeId))

                    result.shouldBeSuccess()
                    coVerify(exactly = 1) {
                        accountMemoPlaceRepository.upsert(
                            account = account,
                            memoId = memoId,
                            placeId = placeId,
                            isDeleted = true,
                            updatedAt = now,
                        )
                    }
                }
            }
        }

        Given("동기화 요청이 실패하도록 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val now = fixtureMonkey.giveMeOne<Instant>()
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = accountUseCase(account = account)
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.failure(throwable)
            val accountMemoPlaceRepository = mockk<AccountMemoPlaceRepository>(relaxed = true)
            val clock = mockk<Clock>()
            every { clock.now() } returns now

            When("장소를 추가한다") {
                Then("TC-MEMO-DETAIL-DATA-043 저장한 장소 연결을 되돌리지 않는다") {
                    val memoId = fixtureMonkey.giveMeOne<Uuid>()
                    val placeId = fixtureMonkey.giveMeOne<Uuid>()
                    val useCase =
                        AddMemoPlaceUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountMemoPlaceRepository = accountMemoPlaceRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = AddMemoPlaceUseCase.Parameter(memoId = memoId, placeId = placeId))

                    result.shouldBeSuccess()
                    coVerify(exactly = 1) {
                        accountMemoPlaceRepository.upsert(account = account, memoId = memoId, placeId = placeId, isDeleted = false, updatedAt = now)
                    }
                    coVerify(exactly = 0) {
                        accountMemoPlaceRepository.upsert(account = account, memoId = memoId, placeId = placeId, isDeleted = true, updatedAt = any())
                    }
                }
            }

            When("장소를 해제한다") {
                Then("TC-MEMO-DETAIL-DATA-043 저장한 장소 연결 해제를 되돌리지 않는다") {
                    val memoId = fixtureMonkey.giveMeOne<Uuid>()
                    val placeId = fixtureMonkey.giveMeOne<Uuid>()
                    val useCase =
                        RemoveMemoPlaceUseCase(
                            getAccountUseCase = getAccountUseCase,
                            requestSyncUseCase = requestSyncUseCase,
                            accountMemoPlaceRepository = accountMemoPlaceRepository,
                            clock = clock,
                        )

                    val result = useCase(parameter = RemoveMemoPlaceUseCase.Parameter(memoId = memoId, placeId = placeId))

                    result.shouldBeSuccess()
                    coVerify(exactly = 1) {
                        accountMemoPlaceRepository.upsert(account = account, memoId = memoId, placeId = placeId, isDeleted = true, updatedAt = now)
                    }
                    coVerify(exactly = 0) {
                        accountMemoPlaceRepository.upsert(account = account, memoId = memoId, placeId = placeId, isDeleted = false, updatedAt = any())
                    }
                }
            }
        }

        Given("저장이 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val accountMemoPlaceRepository = mockk<AccountMemoPlaceRepository>()
            coEvery {
                accountMemoPlaceRepository.upsert(
                    account = any(),
                    memoId = any(),
                    placeId = any(),
                    isDeleted = any(),
                    updatedAt = any(),
                )
            } throws throwable
            val requestSyncUseCase = mockk<RequestSyncUseCase>(relaxed = true)
            val clock = mockk<Clock>(relaxed = true)
            val useCase =
                AddMemoPlaceUseCase(
                    getAccountUseCase = accountUseCase(),
                    requestSyncUseCase = requestSyncUseCase,
                    accountMemoPlaceRepository = accountMemoPlaceRepository,
                    clock = clock,
                )

            When("장소를 추가한다") {
                Then("실패를 그대로 전달한다") {
                    val result =
                        useCase(
                            parameter =
                                AddMemoPlaceUseCase.Parameter(
                                    memoId = fixtureMonkey.giveMeOne<Uuid>(),
                                    placeId = fixtureMonkey.giveMeOne<Uuid>(),
                                ),
                        )

                    result.shouldBeFailure().shouldBeSameInstanceAs(throwable)
                    coVerify(exactly = 0) { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("로그인한 계정과 메모에 연결된 장소가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val memoId = fixtureMonkey.giveMeOne<Uuid>()
            val placeList = List(2) { fixtureMonkey.giveMeOne<Place>() }
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountMemoPlaceRepository = mockk<AccountMemoPlaceRepository>()
            every { accountMemoPlaceRepository.getPlaceList(account = account, memoId = memoId) } returns flowOf(placeList)
            val useCase =
                GetMemoPlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMemoPlaceRepository = accountMemoPlaceRepository,
                )

            When("메모의 장소를 조회한다") {
                Then("현재 계정의 메모에 연결된 장소를 전달한다") {
                    useCase(parameter = memoId).first().shouldBeSuccess(placeList)
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountMemoPlaceRepository = mockk<AccountMemoPlaceRepository>(relaxed = true)
            val useCase =
                GetMemoPlaceUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountMemoPlaceRepository = accountMemoPlaceRepository,
                )

            When("메모의 장소를 조회한다") {
                Then("실패를 그대로 전달한다") {
                    useCase(parameter = fixtureMonkey.giveMeOne<Uuid>())
                        .first()
                        .shouldBeFailure()
                        .shouldBeSameInstanceAs(throwable)
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun accountUseCase(account: Account = fixtureMonkey.giveMeOne<Account.User>()): GetAccountUseCase {
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))

            return getAccountUseCase
        }
    }
}
