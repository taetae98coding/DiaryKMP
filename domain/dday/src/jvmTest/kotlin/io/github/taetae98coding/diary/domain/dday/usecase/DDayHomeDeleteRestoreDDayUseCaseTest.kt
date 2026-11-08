package io.github.taetae98coding.diary.domain.dday.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.dday.repository.AccountDDayRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class DDayHomeDeleteRestoreDDayUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 고정된 현재 시각이 준비되어 있다") {
            val account = Arb.accountUser().next()
            val now = Arb.secondPrecisionInstant().next()
            val requestSyncUseCase = requestSyncUseCase()
            val repository = repository()
            val deleteUseCase = deleteUseCase(getAccountUseCase(Result.success(account)), requestSyncUseCase, repository, clock(now))
            val restoreUseCase = restoreUseCase(getAccountUseCase(Result.success(account)), requestSyncUseCase, repository, clock(now))

            When("목록에서 디데이를 삭제한다") {
                Then("TC-2a38eccb-d6fd-4c10-b3ba-ef1fb0e3e885 TC-ec3e145c-10e3-4f3f-aa03-52f484fd1b0d 현재 계정의 디데이를 삭제와 고정 시각으로 저장한 뒤 동기화를 한 번 요청한다") {
                    val ddayId = Arb.kotlinUuid().next()

                    deleteUseCase(parameter = ddayId).shouldBeSuccess(1)

                    coVerifyOrder {
                        repository.updateDeleted(account = account, ddayId = ddayId, isDeleted = true, updatedAt = now)
                        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                    }
                    coVerify(exactly = 1) { repository.updateDeleted(account = any(), ddayId = any(), isDeleted = any(), updatedAt = any()) }
                    coVerify(exactly = 1) { requestSyncUseCase(parameter = any()) }
                }
            }

            When("삭제를 실행 취소한다") {
                Then("TC-ae40f0f7-9272-43b6-9f35-a2098f5e6abb TC-ec3e145c-10e3-4f3f-aa03-52f484fd1b0d 현재 계정의 디데이를 미삭제와 고정 시각으로 저장한 뒤 동기화를 한 번 요청한다") {
                    val ddayId = Arb.kotlinUuid().next()

                    restoreUseCase(parameter = ddayId).shouldBeSuccess(1)

                    coVerifyOrder {
                        repository.updateDeleted(account = account, ddayId = ddayId, isDeleted = false, updatedAt = now)
                        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                    }
                    coVerify(exactly = 1) { repository.updateDeleted(account = any(), ddayId = ddayId, isDeleted = any(), updatedAt = any()) }
                }
            }
        }

        Given("로그인하지 않아 게스트 계정이 확인된다") {
            val now = Arb.secondPrecisionInstant().next()
            val repository = repository(account = Account.Guest)
            val requestSyncUseCase = requestSyncUseCase()
            val deleteUseCase = deleteUseCase(getAccountUseCase(Result.success(Account.Guest)), requestSyncUseCase, repository, clock(now))
            val restoreUseCase = restoreUseCase(getAccountUseCase(Result.success(Account.Guest)), requestSyncUseCase, repository, clock(now))

            listOf(true to "삭제", false to "실행 취소").forEach { (isDeleted, name) ->
                When("${name}를 저장한다") {
                    Then("TC-31cf25b9-56c6-49f0-acd4-7f6ca2feb139 게스트 계정 기준으로 기기에 저장한다") {
                        val ddayId = Arb.kotlinUuid().next()
                        val useCase = if (isDeleted) deleteUseCase else restoreUseCase

                        useCase(parameter = ddayId).shouldBeSuccess(1)

                        coVerify(exactly = 1) {
                            repository.updateDeleted(account = Account.Guest, ddayId = ddayId, isDeleted = isDeleted, updatedAt = now)
                        }
                    }
                }
            }
        }

        Given("현재 계정을 확인할 수 없다") {
            val throwable = IllegalStateException(Arb.string().next())
            val repository = repository()
            val requestSyncUseCase = requestSyncUseCase()
            val restoreUseCase = restoreUseCase(getAccountUseCase(Result.failure(throwable)), requestSyncUseCase, repository, clock(Arb.secondPrecisionInstant().next()))

            When("삭제를 실행 취소한다") {
                Then("TC-c4afd5d9-8a2a-4ffa-8934-2b0786c16f03 실패를 전달하고 삭제 여부를 저장하지 않으며 동기화도 요청하지 않는다") {
                    restoreUseCase(parameter = Arb.kotlinUuid().next()).shouldBeFailure() shouldBeSameInstanceAs throwable

                    coVerify(exactly = 0) { repository.updateDeleted(account = any(), ddayId = any(), isDeleted = any(), updatedAt = any()) }
                    coVerify(exactly = 0) { requestSyncUseCase(parameter = any()) }
                }
            }
        }
    }) {
    public companion object {
        private fun getAccountUseCase(result: Result<Account>): GetAccountUseCase {
            val useCase = mockk<GetAccountUseCase>()
            every { useCase(parameter = Unit) } returns flowOf(result)
            return useCase
        }

        private fun requestSyncUseCase(): RequestSyncUseCase {
            val useCase = mockk<RequestSyncUseCase>()
            coEvery { useCase(parameter = any()) } returns Result.success(Unit)
            return useCase
        }

        private fun repository(account: Account? = null): AccountDDayRepository {
            val repository = mockk<AccountDDayRepository>()
            coEvery {
                repository.updateDeleted(account = account ?: any(), ddayId = any(), isDeleted = any(), updatedAt = any())
            } returns 1
            return repository
        }

        private fun clock(now: Instant): Clock {
            val clock = mockk<Clock>()
            every { clock.now() } returns now
            return clock
        }

        private fun deleteUseCase(
            getAccountUseCase: GetAccountUseCase,
            requestSyncUseCase: RequestSyncUseCase,
            repository: AccountDDayRepository,
            clock: Clock,
        ): DeleteDDayUseCase =
            DeleteDDayUseCase(
                getAccountUseCase = getAccountUseCase,
                requestSyncUseCase = requestSyncUseCase,
                accountDDayRepository = repository,
                clock = clock,
            )

        private fun restoreUseCase(
            getAccountUseCase: GetAccountUseCase,
            requestSyncUseCase: RequestSyncUseCase,
            repository: AccountDDayRepository,
            clock: Clock,
        ): RestoreDDayUseCase =
            RestoreDDayUseCase(
                getAccountUseCase = getAccountUseCase,
                requestSyncUseCase = requestSyncUseCase,
                accountDDayRepository = repository,
                clock = clock,
            )
    }
}
