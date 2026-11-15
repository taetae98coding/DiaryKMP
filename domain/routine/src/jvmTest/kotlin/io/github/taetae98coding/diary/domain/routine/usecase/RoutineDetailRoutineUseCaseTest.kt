package io.github.taetae98coding.diary.domain.routine.usecase

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.routine.routine
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.routine.RoutineInput
import io.github.taetae98coding.diary.domain.routine.exception.RoutineAmountPerRecordInvalidException
import io.github.taetae98coding.diary.domain.routine.exception.RoutineGoalAmountInvalidException
import io.github.taetae98coding.diary.domain.routine.exception.RoutineUnitInvalidException
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineRepository
import io.github.taetae98coding.diary.domain.routine.routineInput
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class RoutineDetailRoutineUseCaseTest :
    BehaviorSpec({
        Given("현재 시각이 고정되어 있고 로그인한 계정의 루틴이 저장되어 있다") {
            listOf(false, true).forEach { isFinished ->
                When("완료 여부가 ${isFinished}인 루틴을 제목 앞뒤 공백과 줄바꿈 설명으로 수정한다") {
                    val context = DetailContext(stored = Arb.routine(isDeleted = false, isFinished = isFinished).next())
                    val color = Arb.long().next()
                    val input =
                        Arb.routineInput().next().copy(
                            title = " 물 2L ",
                            description = "매일\n꾸준히",
                            color = color,
                            startDate = LocalDate(2026, 2, 1),
                            unit = "m",
                        )
                    val updatedCount = context.update(input = input).shouldBeSuccess()

                    Then("TC-0ad47719-cf1c-40ff-923f-f53f466bd97f 입력한 그대로의 정보와 고정 시각으로 저장한다") {
                        updatedCount shouldBe 1
                        val saved = context.updatedDetailList.single()
                        saved.title shouldBe " 물 2L "
                        saved.description shouldBe "매일\n꾸준히"
                        saved.color shouldBe color
                        saved.startDate shouldBe LocalDate(2026, 2, 1)
                        saved.unit shouldBe "m"
                        coVerify(exactly = 1) {
                            context.repository.updateDetail(account = context.account, routineId = context.routine.id, detail = any(), updatedAt = context.now)
                        }
                    }

                    Then("TC-c20c094f-8b07-4373-aeff-5845f58e9f0b TC-26a743be-a098-43ca-a381-f17a6e065650 정보만 바꾸고 완료 여부와 삭제 여부, 다른 저장은 요청하지 않는다") {
                        coVerify(exactly = 0) { context.repository.updateFinished(account = any(), routineId = any(), isFinished = any(), updatedAt = any()) }
                        coVerify(exactly = 0) { context.repository.updateDeleted(account = any(), routineId = any(), isDeleted = any(), updatedAt = any()) }
                        coVerify(exactly = 0) { context.repository.upsert(account = any(), routine = any(), tagIdSet = any()) }
                    }

                    Then("TC-8887ca96-806e-4958-b37c-6c7601913865 저장한 뒤 동기화를 한 번 요청한다") {
                        coVerifyOrder {
                            context.repository.updateDetail(account = any(), routineId = any(), detail = any(), updatedAt = any())
                            context.requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                        }
                        coVerify(exactly = 1) { context.requestSyncUseCase(parameter = any()) }
                    }
                }
            }

            listOf(
                Triple(RoutineCycle.WEEKLY, 3, 1),
                Triple(RoutineCycle.MONTHLY, 12_000, 500),
                Triple(RoutineCycle.DAILY, null, 9_999_999),
                Triple(RoutineCycle.WEEKDAYS, 20, 5),
                Triple(RoutineCycle.WEEKENDS, null, 1),
            ).forEach { (cycle, goalAmount, amountPerRecord) ->
                When("주기 $cycle, 목표량 $goalAmount, 한 번에 기록할 양 ${amountPerRecord}로 고쳐 수정한다") {
                    val context = DetailContext()
                    val input = Arb.routineInput().next().copy(cycle = cycle, goalAmount = goalAmount, amountPerRecord = amountPerRecord)
                    context.update(input = input).shouldBeSuccess()

                    Then("TC-2dc8c2c6-a0de-4168-bbec-206a8a59d62d 고친 주기와 읽은 수로 저장한다") {
                        val saved = context.updatedDetailList.single()
                        saved.cycle shouldBe cycle
                        saved.goalAmount shouldBe goalAmount
                        saved.amountPerRecord shouldBe amountPerRecord
                    }
                }
            }

            listOf(
                "L" to "L",
                " L " to "L",
                "분 걷기" to "분 걷기",
                "ML" to "ML",
                "" to "",
                "  " to "",
            ).forEachIndexed { index, (unit, expected) ->
                When("$index: 단위를 '$unit'로 고쳐 수정한다") {
                    val context = DetailContext()
                    context.update(input = Arb.routineInput().next().copy(unit = unit)).shouldBeSuccess()

                    Then("TC-97a23bfd-edd0-425c-aa19-eb13804e0ec8 앞뒤 공백을 뺀 글자로 저장한다") {
                        context.updatedDetailList.single().unit shouldBe expected
                    }
                }
            }

            listOf("kilometerss", "밀리\n리터").forEachIndexed { index, unit ->
                When("$index: 10자를 넘거나 줄바꿈이 있는 단위로 고쳐 수정한다") {
                    val context = DetailContext()
                    val result = context.update(input = Arb.routineInput().next().copy(unit = unit))

                    Then("TC-9906df56-12e2-4162-b857-8ddcb290493d 저장하지 않고 단위 오류로 거절한다") {
                        result.shouldBeFailure().shouldBeInstanceOf<RoutineUnitInvalidException>()
                        context.verifyNothingSaved()
                    }
                }
            }

            listOf("", "   ").forEach { title ->
                When("제목을 '$title'로 비운 채 수정한다") {
                    val context = DetailContext()
                    val input = Arb.routineInput().next().copy(title = title)
                    context.update(input = input).shouldBeSuccess()

                    Then("TC-44303e74-098e-47f2-a3a7-8a1a452c3cbc 기존 제목을 유지하고 나머지 정보만 반영한다") {
                        val saved = context.updatedDetailList.single()
                        saved.title shouldBe context.routine.detail.title
                        saved.description shouldBe input.description
                        saved.goalAmount shouldBe input.goalAmount
                        saved.amountPerRecord shouldBe input.amountPerRecord
                    }
                }
            }

            listOf(
                Triple(0, 250, RoutineGoalAmountInvalidException::class),
                Triple(2000, 0, RoutineAmountPerRecordInvalidException::class),
                Triple(null, 0, RoutineAmountPerRecordInvalidException::class),
                Triple(0, 0, RoutineGoalAmountInvalidException::class),
            ).forEach { (goalAmount, amountPerRecord, exceptionClass) ->
                When("목표량 $goalAmount, 한 번에 기록할 양 ${amountPerRecord}로 수정을 요청한다") {
                    val context = DetailContext()
                    val input = Arb.routineInput().next().copy(title = "물 2L", description = "매일", goalAmount = goalAmount, amountPerRecord = amountPerRecord)
                    val result = context.update(input = input)

                    Then("TC-21d49d91-2484-4215-85b3-e37a595e7770 아무 내용도 수정하지 않고 그 사유 하나로 거절한다") {
                        exceptionClass.isInstance(result.shouldBeFailure()) shouldBe true
                        context.verifyNothingSaved()
                    }
                }
            }

            listOf(
                Triple(false, 0 to 0, RoutineGoalAmountInvalidException::class),
                Triple(true, 0 to 250, RoutineGoalAmountInvalidException::class),
                Triple(true, 2000 to 0, RoutineAmountPerRecordInvalidException::class),
                Triple(true, 0 to 0, RoutineGoalAmountInvalidException::class),
            ).forEach { (isTitleEmpty, amount, exceptionClass) ->
                val (goalAmount, amountPerRecord) = amount
                val titleName = if (isTitleEmpty) "빈 제목" else "비어 있지 않은 제목"

                When("$titleName, 목표량 $goalAmount, 한 번에 기록할 양 ${amountPerRecord}로 수정을 요청한다") {
                    val context = DetailContext()
                    val title = if (isTitleEmpty) "" else Arb.nonBlankString().next()
                    val result = context.update(input = Arb.routineInput().next().copy(title = title, goalAmount = goalAmount, amountPerRecord = amountPerRecord))

                    Then("TC-bb687a80-1806-4e35-a60a-950847ffc563 제목을 비웠어도 성립하지 않는 숫자 하나의 사유로 거절한다") {
                        exceptionClass.isInstance(result.shouldBeFailure()) shouldBe true
                        context.verifyNothingSaved()
                    }
                }
            }

            When("루틴을 삭제한다") {
                val context = DetailContext()
                val updatedCount = context.deleteUseCase(parameter = context.routine.id).shouldBeSuccess()

                Then("TC-a7d11800-4d0e-430e-a7bf-51770909236f 삭제 여부와 고정 시각만 저장한다") {
                    updatedCount shouldBe 1
                    coVerify(exactly = 1) {
                        context.repository.updateDeleted(account = context.account, routineId = context.routine.id, isDeleted = true, updatedAt = context.now)
                    }
                    coVerify(exactly = 0) { context.repository.updateDetail(account = any(), routineId = any(), detail = any(), updatedAt = any()) }
                }

                Then("TC-8887ca96-806e-4958-b37c-6c7601913865 삭제를 저장한 뒤 동기화를 한 번 요청한다") {
                    coVerifyOrder {
                        context.repository.updateDeleted(account = any(), routineId = any(), isDeleted = any(), updatedAt = any())
                        context.requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                    }
                    coVerify(exactly = 1) { context.requestSyncUseCase(parameter = any()) }
                }
            }
        }

        Given("로그인하지 않아 게스트 계정이 확인된다") {
            val context = DetailContext(accountResult = Result.success(Account.Guest))

            When("수정과 삭제를 실행한다") {
                context.update(input = Arb.routineInput().next()).shouldBeSuccess()
                context.deleteUseCase(parameter = context.routine.id).shouldBeSuccess()

                Then("TC-24a46e88-852d-48d7-8837-2e9c2dea8338 게스트 계정 기준으로 기기에 저장한다") {
                    coVerify(exactly = 1) { context.repository.updateDetail(account = Account.Guest, routineId = any(), detail = any(), updatedAt = any()) }
                    coVerify(exactly = 1) { context.repository.updateDeleted(account = Account.Guest, routineId = any(), isDeleted = true, updatedAt = any()) }
                }
            }
        }

        Given("기기 저장이 실패한다") {
            val throwable = IllegalStateException(Arb.string().next())
            val context = DetailContext()
            coEvery { context.repository.updateDetail(account = any(), routineId = any(), detail = any(), updatedAt = any()) } throws throwable
            coEvery { context.repository.updateDeleted(account = any(), routineId = any(), isDeleted = any(), updatedAt = any()) } throws throwable

            When("수정과 삭제를 실행한다") {
                val updateResult = context.update(input = Arb.routineInput().next())
                val deleteResult = context.deleteUseCase(parameter = context.routine.id)

                Then("TC-34479437-32c2-4d78-84ce-d20748c95e38 성공으로 다루지 않고 동기화를 요청하지 않는다") {
                    updateResult.shouldBeFailure() shouldBeSameInstanceAs throwable
                    deleteResult.shouldBeFailure() shouldBeSameInstanceAs throwable
                    coVerify(exactly = 0) { context.requestSyncUseCase(parameter = any()) }
                }
            }
        }

        Given("기기 저장은 성공하고 서버 반영을 요청하는 동기화가 실패한다") {
            val context = DetailContext()
            coEvery { context.requestSyncUseCase(parameter = any()) } returns Result.failure(IllegalStateException(Arb.string().next()))

            When("수정과 삭제를 실행한다") {
                val updateResult = context.update(input = Arb.routineInput().next())
                val deleteResult = context.deleteUseCase(parameter = context.routine.id)

                Then("TC-0e6f404f-0e42-4df9-a75a-f94cc5c71169 기기에 저장한 결과로 성공을 전달한다") {
                    updateResult.shouldBeSuccess() shouldBe 1
                    deleteResult.shouldBeSuccess() shouldBe 1
                }
            }
        }

        Given("현재 계정과 연결되지 않았거나 기기에 없는 루틴이다") {
            val context = DetailContext()
            coEvery { context.repository.updateDetail(account = any(), routineId = any(), detail = any(), updatedAt = any()) } returns 0
            coEvery { context.repository.updateDeleted(account = any(), routineId = any(), isDeleted = any(), updatedAt = any()) } returns 0

            When("수정과 삭제를 실행한다") {
                val updateResult = context.update(input = Arb.routineInput().next())
                val deleteResult = context.deleteUseCase(parameter = context.routine.id)

                Then("TC-89c56eb7-db89-42e1-bc90-cd54ef5c636a 바뀐 루틴이 없다고 전달한다") {
                    updateResult.shouldBeSuccess() shouldBe 0
                    deleteResult.shouldBeSuccess() shouldBe 0
                }
            }
        }

        Given("상세 대상 루틴을 조회한다") {
            When("현재 계정을 확인할 수 없다") {
                val throwable = IllegalStateException(Arb.string().next())
                val context = DetailContext(accountResult = Result.failure(throwable))

                Then("TC-59993333-fe5a-4248-84d9-7a7ca26b660e 계정 확인 실패를 전달한다") {
                    context.findUseCase(parameter = context.routine.id).test {
                        awaitItem().shouldBeFailure() shouldBeSameInstanceAs throwable
                        awaitComplete()
                    }
                }
            }

            When("루틴이 다른 계정과만 연결되어 있다") {
                val context = DetailContext(stored = null)

                Then("TC-59993333-fe5a-4248-84d9-7a7ca26b660e 대상 루틴이 없다") {
                    context.findUseCase(parameter = Arb.routine().next().id).test {
                        awaitItem().shouldBeSuccess().shouldBeNull()
                        awaitComplete()
                    }
                }
            }

            When("루틴이 현재 계정과 연결되어 있다") {
                val context = DetailContext()

                Then("TC-59993333-fe5a-4248-84d9-7a7ca26b660e 그 루틴의 정보가 조회된다") {
                    context.findUseCase(parameter = context.routine.id).test {
                        awaitItem().shouldBeSuccess() shouldBe context.routine
                        awaitComplete()
                    }
                }
            }

            When("현재 계정의 루틴이 삭제 상태다") {
                val routine = Arb.routine(isDeleted = true).next()
                val context = DetailContext(stored = routine)

                Then("TC-0ba8553c-17da-4bd4-acf4-b81e0b1cf676 삭제 상태인 루틴도 조회한다") {
                    context.findUseCase(parameter = routine.id).test {
                        awaitItem().shouldBeSuccess() shouldBe routine
                        awaitComplete()
                    }
                }
            }
        }
    })

private class DetailContext(
    accountResult: Result<Account> = Result.success(Arb.accountUser().next()),
    stored: Routine? = Arb.routine(isDeleted = false).next(),
) {
    val account: Account = accountResult.getOrNull() ?: Account.Guest
    val routine: Routine = stored ?: Arb.routine(isDeleted = false).next()
    val now: Instant = Arb.secondPrecisionInstant().next()
    val updatedDetailList: MutableList<RoutineDetail> = mutableListOf()

    val repository: AccountRoutineRepository =
        mockk<AccountRoutineRepository>().also { repository ->
            every { repository.find(account = any(), routineId = any()) } returns flowOf(stored)
            coEvery { repository.updateDetail(account = any(), routineId = any(), detail = any(), updatedAt = any()) } answers {
                updatedDetailList += thirdArg<RoutineDetail>()
                1
            }
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

    val findUseCase: FindRoutineUseCase = FindRoutineUseCase(getAccountUseCase = getAccountUseCase, accountRoutineRepository = repository)

    val updateUseCase: UpdateRoutineUseCase =
        UpdateRoutineUseCase(
            getAccountUseCase = getAccountUseCase,
            requestSyncUseCase = requestSyncUseCase,
            findRoutineUseCase = findUseCase,
            accountRoutineRepository = repository,
            clock = clock,
        )

    val deleteUseCase: DeleteRoutineUseCase =
        DeleteRoutineUseCase(
            getAccountUseCase = getAccountUseCase,
            requestSyncUseCase = requestSyncUseCase,
            accountRoutineRepository = repository,
            clock = clock,
        )

    suspend fun update(
        input: RoutineInput,
        id: Uuid = routine.id,
    ): Result<Int> = updateUseCase(parameter = UpdateRoutineUseCase.Parameter(id = id, input = input))

    fun verifyNothingSaved() {
        coVerify(exactly = 0) { repository.updateDetail(account = any(), routineId = any(), detail = any(), updatedAt = any()) }
        coVerify(exactly = 0) { requestSyncUseCase(parameter = any()) }
    }
}
