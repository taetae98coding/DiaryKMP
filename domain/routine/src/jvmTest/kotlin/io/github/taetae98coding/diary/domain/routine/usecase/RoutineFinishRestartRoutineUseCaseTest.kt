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
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.Uuid

class RoutineFinishRestartRoutineUseCaseTest :
    BehaviorSpec({
        Given("현재 시각이 고정되어 있고 로그인한 계정의 루틴이 저장되어 있다") {
            listOf(true, false).forEach { isFinished ->
                val name = if (isFinished) "완료" else "다시 시작"

                When("루틴의 ${name}을 저장한다") {
                    val context = FinishRestartContext()
                    val routineId = Arb.kotlinUuid().next()
                    val updatedCount = context.execute(routineId = routineId, isFinished = isFinished).shouldBeSuccess()

                    Then("TC-1f95e299-8738-4411-a9c0-7efec0662367 TC-44da985b-f646-490d-8f22-a744f33a8690 TC-bbb6db55-01ee-4859-928c-5b2005951fbe 완료 여부와 고정 시각만 저장한다") {
                        updatedCount shouldBe 1
                        coVerify(exactly = 1) {
                            context.repository.updateFinished(account = context.account, routineId = routineId, isFinished = isFinished, updatedAt = context.now)
                        }
                        context.verifyOnlyFinishedSaved()
                    }

                    Then("TC-0ac61b76-833f-4f81-b9a6-95f2bf19cb22 TC-b4d82a29-6178-41a9-8cc5-72a10eb30be7 TC-774c684c-1158-43f4-94d1-0cf7e4a7fb92 루틴을 지우지 않고 동작 시점으로 저장한다") {
                        coVerify(exactly = 1) { context.repository.updateFinished(account = any(), routineId = routineId, isFinished = isFinished, updatedAt = context.now) }
                        coVerify(exactly = 0) { context.repository.updateDeleted(account = any(), routineId = any(), isDeleted = any(), updatedAt = any()) }
                    }

                    Then("TC-6d3fa494-c938-45d6-9274-1bd2f4ec22d5 TC-54395f83-6c01-4480-ba23-2a5720591d12 저장한 뒤 동기화를 한 번 요청한다") {
                        coVerifyOrder {
                            context.repository.updateFinished(account = any(), routineId = any(), isFinished = any(), updatedAt = any())
                            context.requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                        }
                        coVerify(exactly = 1) { context.requestSyncUseCase(parameter = any()) }
                    }
                }
            }
        }

        Given("수정 시각이 시각 A인 완료되지 않은 루틴이 저장되어 있다") {
            val context = FinishRestartContext()
            val routineId = Arb.kotlinUuid().next()
            val finishedAt = context.now + 1.seconds
            val restartedAt = finishedAt + 1.seconds
            every { context.clock.now() } returnsMany listOf(finishedAt, restartedAt)

            When("시각 B에 완료하고 시각 C에 다시 시작한다") {
                context.execute(routineId = routineId, isFinished = true).shouldBeSuccess()
                context.execute(routineId = routineId, isFinished = false).shouldBeSuccess()

                Then("TC-4c8d139c-50a3-4d73-834f-2f6af23f7ffb 완료는 시각 B로, 다시 시작은 시각 C로 저장한다") {
                    coVerifyOrder {
                        context.repository.updateFinished(account = context.account, routineId = routineId, isFinished = true, updatedAt = finishedAt)
                        context.repository.updateFinished(account = context.account, routineId = routineId, isFinished = false, updatedAt = restartedAt)
                    }
                    context.verifyOnlyFinishedSaved()
                }
            }
        }

        Given("기기 저장이 실패한다") {
            listOf(true, false).forEach { isFinished ->
                val throwable = IllegalStateException(Arb.string().next())
                val context = FinishRestartContext()
                coEvery { context.repository.updateFinished(account = any(), routineId = any(), isFinished = any(), updatedAt = any()) } throws throwable

                When("완료 여부 ${isFinished}를 저장한다") {
                    val result = context.execute(routineId = Arb.kotlinUuid().next(), isFinished = isFinished)

                    Then("TC-6c70fa47-52f4-4b00-b897-c505b3706104 성공으로 다루지 않고 동기화를 요청하지 않는다") {
                        result.shouldBeFailure() shouldBeSameInstanceAs throwable
                        coVerify(exactly = 0) { context.requestSyncUseCase(parameter = any()) }
                    }
                }
            }
        }

        Given("기기 저장은 성공하고 서버 반영을 요청하는 동기화가 실패한다") {
            listOf(true, false).forEach { isFinished ->
                val context = FinishRestartContext()
                coEvery { context.requestSyncUseCase(parameter = any()) } returns Result.failure(IllegalStateException(Arb.string().next()))

                When("완료 여부 ${isFinished}를 저장한다") {
                    val result = context.execute(routineId = Arb.kotlinUuid().next(), isFinished = isFinished)

                    Then("TC-fdb65d97-e4b7-40fb-839b-aec32b74dc98 기기에 저장한 결과로 성공을 전달한다") {
                        result.shouldBeSuccess() shouldBe 1
                        coVerify(exactly = 1) { context.repository.updateFinished(account = any(), routineId = any(), isFinished = isFinished, updatedAt = any()) }
                    }
                }
            }
        }

        Given("대상 루틴이 현재 계정과 연결되어 있지 않거나 기기에 없다") {
            val context = FinishRestartContext()
            coEvery { context.repository.updateFinished(account = any(), routineId = any(), isFinished = any(), updatedAt = any()) } returns 0

            When("완료와 다시 시작을 저장한다") {
                val routineId = Arb.kotlinUuid().next()
                val finishResult = context.execute(routineId = routineId, isFinished = true)
                val restartResult = context.execute(routineId = routineId, isFinished = false)

                Then("TC-a67df1e5-b918-49da-85e7-b6a1308e808d 바뀐 루틴이 없다고 전달한다") {
                    finishResult.shouldBeSuccess() shouldBe 0
                    restartResult.shouldBeSuccess() shouldBe 0
                    context.verifyOnlyFinishedSaved()
                }
            }
        }

        Given("현재 계정을 확인할 수 없다") {
            val throwable = IllegalStateException(Arb.string().next())
            val context = FinishRestartContext(accountResult = Result.failure(throwable))

            When("완료와 다시 시작을 저장한다") {
                val finishResult = context.execute(routineId = Arb.kotlinUuid().next(), isFinished = true)
                val restartResult = context.execute(routineId = Arb.kotlinUuid().next(), isFinished = false)

                Then("저장하지 않고 실패를 전달한다") {
                    finishResult.shouldBeFailure() shouldBeSameInstanceAs throwable
                    restartResult.shouldBeFailure() shouldBeSameInstanceAs throwable
                    coVerify(exactly = 0) { context.repository.updateFinished(account = any(), routineId = any(), isFinished = any(), updatedAt = any()) }
                    coVerify(exactly = 0) { context.requestSyncUseCase(parameter = any()) }
                }
            }
        }

        Given("로그인하지 않아 게스트 계정이 확인된다") {
            val context = FinishRestartContext(accountResult = Result.success(Account.Guest))

            When("완료와 다시 시작을 저장한다") {
                val routineId = Arb.kotlinUuid().next()
                context.execute(routineId = routineId, isFinished = true).shouldBeSuccess()
                context.execute(routineId = routineId, isFinished = false).shouldBeSuccess()

                Then("TC-c73d0aa4-86f5-4ef5-8f10-8fd64f7f8c57 게스트 계정 기준으로 기기에 저장한다") {
                    coVerify(exactly = 1) { context.repository.updateFinished(account = Account.Guest, routineId = routineId, isFinished = true, updatedAt = any()) }
                    coVerify(exactly = 1) { context.repository.updateFinished(account = Account.Guest, routineId = routineId, isFinished = false, updatedAt = any()) }
                }
            }
        }
    })

private class FinishRestartContext(
    accountResult: Result<Account> = Result.success(Arb.accountUser().next()),
) {
    val account: Account = accountResult.getOrNull() ?: Account.Guest
    val now: Instant = Arb.secondPrecisionInstant().next()

    val repository: AccountRoutineRepository =
        mockk<AccountRoutineRepository>().also { repository ->
            coEvery { repository.updateFinished(account = any(), routineId = any(), isFinished = any(), updatedAt = any()) } returns 1
        }

    val requestSyncUseCase: RequestSyncUseCase =
        mockk<RequestSyncUseCase>().also { useCase ->
            coEvery { useCase(parameter = any()) } returns Result.success(Unit)
        }

    val clock: Clock =
        mockk<Clock>().also { clock ->
            every { clock.now() } returns now
        }

    private val getAccountUseCase: GetAccountUseCase =
        mockk<GetAccountUseCase>().also { useCase ->
            every { useCase() } returns flowOf(accountResult)
        }

    private val finishUseCase: FinishRoutineUseCase =
        FinishRoutineUseCase(getAccountUseCase = getAccountUseCase, requestSyncUseCase = requestSyncUseCase, accountRoutineRepository = repository, clock = clock)

    private val restartUseCase: RestartRoutineUseCase =
        RestartRoutineUseCase(getAccountUseCase = getAccountUseCase, requestSyncUseCase = requestSyncUseCase, accountRoutineRepository = repository, clock = clock)

    suspend fun execute(
        routineId: Uuid,
        isFinished: Boolean,
    ): Result<Int> = if (isFinished) finishUseCase(parameter = routineId) else restartUseCase(parameter = routineId)

    // 완료와 다시 시작은 루틴의 다른 정보와 삭제 여부를 저장하는 연산을 부르지 않는다.
    fun verifyOnlyFinishedSaved() {
        coVerify(exactly = 0) { repository.upsert(account = any(), routine = any(), tagIdSet = any()) }
        coVerify(exactly = 0) { repository.updateDetail(account = any(), routineId = any(), detail = any(), updatedAt = any()) }
        coVerify(exactly = 0) { repository.updateDeleted(account = any(), routineId = any(), isDeleted = any(), updatedAt = any()) }
    }
}
