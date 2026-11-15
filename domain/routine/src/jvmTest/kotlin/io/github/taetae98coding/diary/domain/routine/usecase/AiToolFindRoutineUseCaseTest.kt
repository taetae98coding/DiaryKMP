package io.github.taetae98coding.diary.domain.routine.usecase

import app.cash.turbine.test
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.routine.routineRecord
import io.github.taetae98coding.diary.core.testing.routine.routineSummary
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.routine.progress.routineCurrentCycle
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineRecordRepository
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineRepository
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf

class AiToolFindRoutineUseCaseTest :
    BehaviorSpec({
        Given("현재 계정의 루틴과 이번 주기의 합계가 준비되어 있다") {
            val account = Arb.accountUser().next()
            val today = Arb.kotlinxLocalDate().next()
            val routineId = Arb.kotlinUuid().next()
            val summary = Arb.routineSummary().orNull().next()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.success(account))
            val repository = mockk<AccountRoutineRepository>()
            every { repository.findSummary(account = account, routineId = routineId, currentCycle = routineCurrentCycle(today = today)) } returns flowOf(summary)
            val useCase = FindRoutineSummaryUseCase(getAccountUseCase = getAccountUseCase, accountRoutineRepository = repository)

            When("루틴과 오늘을 넘겨 루틴 하나를 합계와 함께 찾는다") {
                Then("TC-68c32f9f-68c5-4b5c-8ac5-74e6e6bff138 오늘이 속한 이번 주기로 찾은 현재 계정의 루틴과 합계를 반환한다") {
                    useCase(parameter = FindRoutineSummaryUseCase.Parameter(routineId = routineId, today = today)).test {
                        awaitItem().getOrThrow() shouldBe summary
                        awaitComplete()
                    }
                }
            }
        }

        Given("현재 계정의 루틴 기록이 준비되어 있다") {
            val account = Arb.accountUser().next()
            val recordId = Arb.kotlinUuid().next()
            val record = Arb.routineRecord().orNull().next()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.success(account))
            val repository = mockk<AccountRoutineRecordRepository>()
            every { repository.find(account = account, recordId = recordId) } returns flowOf(record)
            val useCase = FindRoutineRecordUseCase(getAccountUseCase = getAccountUseCase, accountRoutineRecordRepository = repository)

            When("기록 하나를 찾는다") {
                Then("현재 계정의 그 기록을 반환한다") {
                    useCase(parameter = recordId).test {
                        awaitItem().getOrThrow() shouldBe record
                        awaitComplete()
                    }
                }
            }
        }

        Given("현재 계정 조회가 실패하도록 설정되어 있다") {
            val failure = IllegalStateException(Arb.string().next())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.failure(failure))
            val routineRepository = mockk<AccountRoutineRepository>(relaxed = true)
            val recordRepository = mockk<AccountRoutineRecordRepository>(relaxed = true)

            When("루틴 하나를 합계와 함께 찾거나 기록 하나를 찾는다") {
                Then("TC-78e9deb8-0990-4954-9903-4337415a5943 조회하지 않고 계정 조회의 실패를 그대로 전달한다") {
                    FindRoutineSummaryUseCase(getAccountUseCase = getAccountUseCase, accountRoutineRepository = routineRepository)(
                        parameter = FindRoutineSummaryUseCase.Parameter(routineId = Arb.kotlinUuid().next(), today = Arb.kotlinxLocalDate().next()),
                    ).test {
                        awaitItem().shouldBeFailure { throwable -> throwable shouldBe failure }
                        awaitComplete()
                    }
                    FindRoutineRecordUseCase(getAccountUseCase = getAccountUseCase, accountRoutineRecordRepository = recordRepository)(parameter = Arb.kotlinUuid().next()).test {
                        awaitItem().shouldBeFailure { throwable -> throwable shouldBe failure }
                        awaitComplete()
                    }

                    verify(exactly = 0) { routineRepository.findSummary(account = any(), routineId = any(), currentCycle = any()) }
                    verify(exactly = 0) { recordRepository.find(account = any(), recordId = any()) }
                }
            }
        }
    })
