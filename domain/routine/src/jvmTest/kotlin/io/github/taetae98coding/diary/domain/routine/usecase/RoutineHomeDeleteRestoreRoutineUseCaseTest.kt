package io.github.taetae98coding.diary.domain.routine.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
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

class RoutineHomeDeleteRestoreRoutineUseCaseTest :
    BehaviorSpec({
        Given("현재 시각이 고정되어 있고 로그인한 계정의 루틴이 저장되어 있다") {
            listOf(true, false).forEach { isDeleted ->
                val name = if (isDeleted) "삭제" else "실행 취소"

                When("루틴의 ${name}를 저장한다") {
                    val context = DeleteRestoreContext()
                    val routineId = Arb.kotlinUuid().next()
                    val updatedCount = context.execute(routineId = routineId, isDeleted = isDeleted).shouldBeSuccess()

                    Then("TC-1336b40c-0a9f-43f3-be56-23a72bb05de3 TC-5585c876-466c-4887-8d63-9c54b0c6a7d5 현재 계정 루틴의 삭제 여부와 고정 시각만 저장한다") {
                        updatedCount shouldBe 1
                        coVerify(exactly = 1) {
                            context.repository.updateDeleted(account = context.account, routineId = routineId, isDeleted = isDeleted, updatedAt = context.now)
                        }
                        coVerify(exactly = 0) { context.repository.updateDetail(account = any(), routineId = any(), detail = any(), updatedAt = any()) }
                    }

                    Then("TC-091631e1-6514-4f40-b33a-87d5eeb743a4 저장한 뒤 동기화를 한 번 요청한다") {
                        coVerifyOrder {
                            context.repository.updateDeleted(account = any(), routineId = any(), isDeleted = any(), updatedAt = any())
                            context.requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                        }
                        coVerify(exactly = 1) { context.requestSyncUseCase(parameter = any()) }
                    }
                }
            }
        }

        Given("현재 계정을 확인할 수 없다") {
            val throwable = IllegalStateException(Arb.string().next())
            val context = DeleteRestoreContext(accountResult = Result.failure(throwable))

            When("삭제의 실행 취소를 저장한다") {
                val result = context.execute(routineId = Arb.kotlinUuid().next(), isDeleted = false)

                Then("TC-2c80b13e-8a77-4748-a317-77b7950255ff 저장하지 않고 실패를 전달한다") {
                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                    coVerify(exactly = 0) { context.repository.updateDeleted(account = any(), routineId = any(), isDeleted = any(), updatedAt = any()) }
                    coVerify(exactly = 0) { context.requestSyncUseCase(parameter = any()) }
                }
            }
        }

        Given("로그인하지 않아 게스트 계정이 확인된다") {
            val context = DeleteRestoreContext(accountResult = Result.success(Account.Guest))

            When("삭제와 실행 취소를 저장한다") {
                val routineId = Arb.kotlinUuid().next()
                context.execute(routineId = routineId, isDeleted = true).shouldBeSuccess()
                context.execute(routineId = routineId, isDeleted = false).shouldBeSuccess()

                Then("TC-5d2c34e1-fd3e-4b4e-9075-0357e5ceee91 게스트 계정 기준으로 기기에 저장한다") {
                    coVerify(exactly = 1) { context.repository.updateDeleted(account = Account.Guest, routineId = routineId, isDeleted = true, updatedAt = any()) }
                    coVerify(exactly = 1) { context.repository.updateDeleted(account = Account.Guest, routineId = routineId, isDeleted = false, updatedAt = any()) }
                }
            }
        }
    })

private class DeleteRestoreContext(
    accountResult: Result<Account> = Result.success(Arb.accountUser().next()),
) {
    val account: Account = accountResult.getOrNull() ?: Account.Guest
    val now: Instant = Arb.secondPrecisionInstant().next()

    val repository: AccountRoutineRepository =
        mockk<AccountRoutineRepository>().also { repository ->
            coEvery { repository.updateDeleted(account = any(), routineId = any(), isDeleted = any(), updatedAt = any()) } returns 1
        }

    val requestSyncUseCase: RequestSyncUseCase =
        mockk<RequestSyncUseCase>().also { useCase ->
            coEvery { useCase(parameter = any()) } returns Result.success(Unit)
        }

    private val getAccountUseCase: GetAccountUseCase =
        mockk<GetAccountUseCase>().also { useCase ->
            every { useCase() } returns flowOf(accountResult)
        }

    private val clock: Clock =
        mockk<Clock>().also { clock ->
            every { clock.now() } returns now
        }

    private val deleteUseCase: DeleteRoutineUseCase =
        DeleteRoutineUseCase(getAccountUseCase = getAccountUseCase, requestSyncUseCase = requestSyncUseCase, accountRoutineRepository = repository, clock = clock)

    private val restoreUseCase: RestoreRoutineUseCase =
        RestoreRoutineUseCase(getAccountUseCase = getAccountUseCase, requestSyncUseCase = requestSyncUseCase, accountRoutineRepository = repository, clock = clock)

    suspend fun execute(
        routineId: Uuid,
        isDeleted: Boolean,
    ): Result<Int> = if (isDeleted) deleteUseCase(parameter = routineId) else restoreUseCase(parameter = routineId)
}
