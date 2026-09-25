package io.github.taetae98coding.diary.domain.tag.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.domain.tag.repository.AccountTagRepository
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

class RestartTagUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 현재 시각이 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val now = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val accountTagRepository = mockk<AccountTagRepository>()
            coEvery {
                accountTagRepository.updateFinished(account = account, tagId = any(), isFinished = any(), updatedAt = any())
            } returns 1
            val clock = mockk<Clock>()
            every { clock.now() } returns now
            val useCase =
                RestartTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountTagRepository = accountTagRepository,
                    clock = clock,
                )

            When("태그 완료를 실행 취소한다") {
                Then("TC-TAG-DETAIL-DATA-002 현재 계정의 태그를 미완료 상태와 동작 시점 수정 시각으로 되돌린다") {
                    val tagId = fixtureMonkey.giveMeOne<Uuid>()

                    val result = useCase(parameter = tagId)

                    result.shouldBeSuccess(1)
                    coVerify(exactly = 1) {
                        accountTagRepository.updateFinished(account = account, tagId = tagId, isFinished = false, updatedAt = now)
                    }
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            val accountTagRepository = mockk<AccountTagRepository>(relaxed = true)
            val clock = mockk<Clock>(relaxed = true)
            val useCase =
                RestartTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountTagRepository = accountTagRepository,
                    clock = clock,
                )

            When("태그 완료 실행 취소를 실행한다") {
                Then("계정 조회 실패를 전달하고 태그 상태를 변경하지 않는다") {
                    val result = useCase(parameter = fixtureMonkey.giveMeOne<Uuid>())

                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                    coVerify(exactly = 0) {
                        accountTagRepository.updateFinished(account = any(), tagId = any(), isFinished = any(), updatedAt = any())
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
            val accountTagRepository = mockk<AccountTagRepository>()
            coEvery {
                accountTagRepository.updateFinished(account = account, tagId = any(), isFinished = any(), updatedAt = any())
            } returns 1
            val clock = mockk<Clock>()
            every { clock.now() } returns Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())
            val useCase =
                RestartTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    requestSyncUseCase = requestSyncUseCase,
                    accountTagRepository = accountTagRepository,
                    clock = clock,
                )

            When("태그 완료를 실행 취소한다") {
                Then("TC-SYNC-REFRESH-FEATURE-004 태그 갱신 후 동기화를 한 번 요청한다") {
                    val result = useCase(parameter = fixtureMonkey.giveMeOne<Uuid>())

                    result.shouldBeSuccess(1)
                    coVerifyOrder {
                        accountTagRepository.updateFinished(account = account, tagId = any(), isFinished = false, updatedAt = any())
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
