package io.github.taetae98coding.diary.domain.routine.usecase

import androidx.paging.PagingData
import app.cash.turbine.test
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.routine.routine
import io.github.taetae98coding.diary.core.testing.routine.routineDetail
import io.github.taetae98coding.diary.core.testing.routine.routineRecord
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.routine.exception.RoutineNotFoundException
import io.github.taetae98coding.diary.domain.routine.exception.RoutineRecordAmountInvalidException
import io.github.taetae98coding.diary.domain.routine.exception.RoutineRecordDateOutOfRangeException
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineRecordRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.uuid.Uuid

// 오늘보다 뒤의 날짜를 만드는, 시작 날짜부터의 일수다. 진행 중인 루틴의 시작 날짜는 오늘보다 30일 이내로 앞선다.
private const val FUTURE_DAY_COUNT = 365

class RoutineDetailRoutineRecordUseCaseTest :
    BehaviorSpec({
        Given("현재 시각이 고정되어 있고 현재 계정의 루틴이 저장되어 있다") {
            listOf(500, 1, 9_999_999).forEach { amount ->
                When("오늘이 아닌 기록할 수 있는 날짜에 양 ${amount}의 기록을 넣는다") {
                    val context = RoutineRecordContext()
                    val date = context.today.minus(1, DateTimeUnit.DAY)
                    val id = context.add(date = date, amount = amount).shouldBeSuccess()

                    Then("TC-aaa79d00-61de-4f2c-969a-40589d5decd4 TC-4c532680-5a51-4c03-a37d-bee4981c14c8 고른 날짜와 양의 삭제되지 않은 새 기록을 현재 계정에 저장한다") {
                        val saved = context.savedList.single()
                        saved.id shouldBe id
                        saved.routineId shouldBe checkNotNull(context.stored.value).id
                        saved.date shouldBe date
                        saved.amount shouldBe amount
                        saved.isDeleted shouldBe false
                        saved.createdAt shouldBe context.now
                        saved.updatedAt shouldBe context.now
                        coVerify(exactly = 1) { context.recordRepository.upsert(account = context.account, record = any()) }
                        context.verifyRoutineUntouched()
                    }

                    Then("TC-8887ca96-806e-4958-b37c-6c7601913865 저장한 뒤 동기화를 한 번 요청한다") {
                        coVerifyOrder {
                            context.recordRepository.upsert(account = any(), record = any())
                            context.requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                        }
                        coVerify(exactly = 1) { context.requestSyncUseCase(parameter = any()) }
                    }
                }
            }

            listOf(0 to false, 1 to true, 9_999_999 to true, 10_000_000 to false).forEach { (amount, isValid) ->
                When("양 ${amount}의 기록 넣기를 요청한다") {
                    val context = RoutineRecordContext()
                    val result = context.add(date = context.today, amount = amount)

                    Then("TC-8f20a50e-651a-42a3-9276-61ff22c596a3 TC-2c877d6a-a7d4-477f-9db0-a4c7e39d20f4 양이 1 이상 9,999,999 이하일 때만 기록이 저장된다") {
                        result.isSuccess shouldBe isValid
                        context.savedList.size shouldBe if (isValid) 1 else 0
                        if (!isValid) {
                            result.shouldBeFailure().shouldBeInstanceOf<RoutineRecordAmountInvalidException>()
                            context.verifyNothingSaved()
                        }
                    }
                }
            }

            listOf<Triple<String, (RoutineDetail) -> LocalDate, Boolean>>(
                Triple("시작 날짜 하루 전", { detail -> detail.startDate.minus(1, DateTimeUnit.DAY) }, false),
                Triple("시작 날짜", { detail -> detail.startDate }, true),
                Triple("오늘보다 뒤", { detail -> detail.startDate.plus(FUTURE_DAY_COUNT, DateTimeUnit.DAY) }, true),
            ).forEach { (name, dateOf, canRecord) ->
                When("$name 날짜의 기록 넣기를 요청한다") {
                    val context = RoutineRecordContext()
                    val date = dateOf(checkNotNull(context.stored.value).detail)
                    val result = context.add(date = date, amount = 1)

                    Then("TC-c158cc96-e709-482b-afb6-a0a08e36efb6 시작 날짜와 같거나 그 뒤인 날짜에만 기록이 저장된다") {
                        result.isSuccess shouldBe canRecord
                        if (!canRecord) {
                            result.shouldBeFailure().shouldBeInstanceOf<RoutineRecordDateOutOfRangeException>()
                            context.verifyNothingSaved()
                        }
                    }
                }
            }

            When("달성률이 100인 루틴에 오늘 날짜로 양 1의 기록을 넣는다") {
                val context = RoutineRecordContext().also { context -> context.stored.value = checkNotNull(context.stored.value).let { routine -> routine.copy(detail = routine.detail.copy(goalAmount = 3)) } }
                val result = context.add(date = context.today, amount = 1)

                Then("TC-4210b8a5-ba71-4a36-b7dc-672982539931 목표를 채웠는지와 관계없이 기록이 넣어진다") {
                    result.shouldBeSuccess()
                    context.savedList.single().amount shouldBe 1
                }
            }

            When("기록 하나를 뺀다") {
                val context = RoutineRecordContext()
                val recordId = Arb.kotlinUuid().next()
                val updatedCount = context.deleteUseCase(parameter = recordId).shouldBeSuccess()

                Then("TC-66ca9007-c763-4a22-8cf6-66a583355e88 TC-da48468e-ae2a-45bd-b406-de0dac89755d 그 기록의 삭제 여부와 고정 시각만 저장한다") {
                    updatedCount shouldBe 1
                    coVerify(exactly = 1) {
                        context.recordRepository.updateDeleted(account = context.account, recordId = recordId, isDeleted = true, updatedAt = context.now)
                    }
                    coVerify(exactly = 0) { context.recordRepository.upsert(account = any(), record = any()) }
                    context.verifyRoutineUntouched()
                }

                Then("TC-8887ca96-806e-4958-b37c-6c7601913865 TC-d513ab4c-84b3-4ea4-8309-6d39e08fc80e 저장한 뒤 동기화를 한 번 요청한다") {
                    coVerifyOrder {
                        context.recordRepository.updateDeleted(account = any(), recordId = any(), isDeleted = any(), updatedAt = any())
                        context.requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                    }
                    coVerify(exactly = 1) { context.requestSyncUseCase(parameter = any()) }
                }
            }
        }

        Given("시작 날짜가 2026-11-01인 루틴에 2026-11-05의 기록을 넣은 뒤 시작 날짜를 2026-11-10으로 바꿔 저장했다") {
            val context =
                RoutineRecordContext(
                    routine = Arb.routine(isDeleted = false, detail = Arb.routineDetail(startDate = LocalDate(2026, 11, 1), cycle = RoutineCycle.MONTHLY).next()).next(),
                )
            context.add(date = LocalDate(2026, 11, 5), amount = 300).shouldBeSuccess()
            context.stored.value = checkNotNull(context.stored.value).let { routine -> routine.copy(detail = routine.detail.copy(startDate = LocalDate(2026, 11, 10))) }

            When("2026-11-05 날짜에 새 기록 넣기를 요청한다") {
                val result = context.add(date = LocalDate(2026, 11, 5), amount = 300)

                Then("TC-3ba53fe9-94b9-495b-895b-c7ae224eec65 범위를 벗어난 날짜라 새 기록을 넣을 수 없고 이미 넣은 기록은 그대로다") {
                    result.shouldBeFailure().shouldBeInstanceOf<RoutineRecordDateOutOfRangeException>()
                    context.savedList.map { record -> record.date to record.amount } shouldBe listOf(LocalDate(2026, 11, 5) to 300)
                }
            }
        }

        Given("현재 계정과 연결된 루틴이 삭제 상태로 저장되어 있다") {
            val context = RoutineRecordContext().also { context -> context.stored.value = checkNotNull(context.stored.value).copy(isDeleted = true) }

            When("기록을 넣고 뺀다") {
                val addResult = context.add(date = context.today, amount = 1)
                val deleteResult = context.deleteUseCase(parameter = Arb.kotlinUuid().next())

                Then("TC-f16b6610-5540-40f1-82d4-9ba2780658ea TC-8b41ff69-57ea-4181-88d1-370b18d77ab6 기록 넣기와 빼기가 반영되고 루틴의 삭제 여부는 바꾸지 않는다") {
                    addResult.shouldBeSuccess() shouldBe context.savedList.single().id
                    deleteResult.shouldBeSuccess() shouldBe 1
                    context.verifyRoutineUntouched()
                }
            }
        }

        Given("현재 계정과 연결된 루틴이 완료 상태로 저장되어 있다") {
            listOf<Triple<String, (RoutineDetail) -> LocalDate, Boolean>>(
                Triple("시작 날짜 하루 전", { detail -> detail.startDate.minus(1, DateTimeUnit.DAY) }, false),
                Triple("시작 날짜", { detail -> detail.startDate }, true),
                Triple("오늘보다 뒤", { detail -> detail.startDate.plus(FUTURE_DAY_COUNT, DateTimeUnit.DAY) }, true),
            ).forEach { (name, dateOf, canRecord) ->
                When("$name 날짜의 기록 넣기를 요청한다") {
                    val context = RoutineRecordContext().also { context -> context.stored.value = inProgressRoutine(today = context.today, isFinished = true) }
                    val result = context.add(date = dateOf(checkNotNull(context.stored.value).detail), amount = 1)

                    Then("TC-3c883f41-9bbb-4b3e-b611-d4b439ba1ca4 TC-628f5f57-cf32-443a-b20a-df931cc5b5a8 완료되지 않은 루틴과 같은 기준으로 저장된다") {
                        result.isSuccess shouldBe canRecord
                        context.savedList.size shouldBe if (canRecord) 1 else 0
                        if (!canRecord) result.shouldBeFailure().shouldBeInstanceOf<RoutineRecordDateOutOfRangeException>()
                    }
                }
            }

            When("기록을 넣고 뺀다") {
                val context = RoutineRecordContext().also { context -> context.stored.value = inProgressRoutine(today = context.today, isFinished = true) }
                val addResult = context.add(date = context.today, amount = 1)
                val deleteResult = context.deleteUseCase(parameter = Arb.kotlinUuid().next())

                Then("TC-d8b54831-56d1-44ff-8a5e-24ae97c4b065 TC-ebd9deee-73ac-4b54-9806-3bb3e9f9c38a 기록 넣기와 빼기가 반영되고 루틴의 완료 여부는 바꾸지 않는다") {
                    addResult.shouldBeSuccess() shouldBe context.savedList.single().id
                    deleteResult.shouldBeSuccess() shouldBe 1
                    coVerify(exactly = 1) { context.recordRepository.updateDeleted(account = context.account, recordId = any(), isDeleted = true, updatedAt = context.now) }
                    context.verifyRoutineUntouched()
                }
            }
        }

        Given("루틴이 다른 계정과만 연결되어 있거나 기기에 없다") {
            val context = RoutineRecordContext(routine = null)
            coEvery { context.recordRepository.updateDeleted(account = any(), recordId = any(), isDeleted = any(), updatedAt = any()) } returns 0

            When("기록을 넣고 뺀다") {
                val addResult = context.addUseCase(parameter = AddRoutineRecordUseCase.Parameter(routineId = Arb.kotlinUuid().next(), date = context.today, amount = 1))
                val deleteResult = context.deleteUseCase(parameter = Arb.kotlinUuid().next())

                Then("TC-89c56eb7-db89-42e1-bc90-cd54ef5c636a 기록이 저장되지 않고 바뀐 기록이 없다고 전달한다") {
                    addResult.shouldBeFailure().shouldBeInstanceOf<RoutineNotFoundException>()
                    coVerify(exactly = 0) { context.recordRepository.upsert(account = any(), record = any()) }
                    deleteResult.shouldBeSuccess() shouldBe 0
                }
            }
        }

        Given("기록을 저장할 때 루틴이 계정과 연결되어 있지 않다") {
            val context = RoutineRecordContext()
            coEvery { context.recordRepository.upsert(account = any(), record = any()) } returns 0

            When("기록을 넣는다") {
                val result = context.add(date = context.today, amount = 1)

                Then("TC-89c56eb7-db89-42e1-bc90-cd54ef5c636a 성공으로 다루지 않고 동기화를 요청하지 않는다") {
                    result.shouldBeFailure().shouldBeInstanceOf<IllegalStateException>()
                    coVerify(exactly = 0) { context.requestSyncUseCase(parameter = any()) }
                }
            }
        }

        Given("로그인하지 않아 게스트 계정이 확인된다") {
            val context = RoutineRecordContext(accountResult = Result.success(Account.Guest))

            When("기록을 넣고 뺀다") {
                context.add(date = context.today, amount = 1).shouldBeSuccess()
                context.deleteUseCase(parameter = Arb.kotlinUuid().next()).shouldBeSuccess()

                Then("TC-24a46e88-852d-48d7-8837-2e9c2dea8338 TC-b2068d06-812b-4553-a5c3-a6cb17f5b483 게스트 계정 기준으로 기기에 저장한다") {
                    coVerify(exactly = 1) { context.recordRepository.upsert(account = Account.Guest, record = any()) }
                    coVerify(exactly = 1) { context.recordRepository.updateDeleted(account = Account.Guest, recordId = any(), isDeleted = true, updatedAt = any()) }
                }
            }
        }

        Given("현재 계정을 확인할 수 없다") {
            val throwable = IllegalStateException(Arb.string().next())
            val context = RoutineRecordContext(accountResult = Result.failure(throwable))

            When("기록을 넣고 뺀다") {
                val addResult = context.add(date = context.today, amount = 1)
                val deleteResult = context.deleteUseCase(parameter = Arb.kotlinUuid().next())

                Then("TC-2b60a7e5-af87-4138-8500-bd361b035fa8 기록이 생기지 않고 실패를 전달한다") {
                    addResult.shouldBeFailure() shouldBeSameInstanceAs throwable
                    deleteResult.shouldBeFailure() shouldBeSameInstanceAs throwable
                    context.verifyNothingSaved()
                    coVerify(exactly = 0) { context.recordRepository.updateDeleted(account = any(), recordId = any(), isDeleted = any(), updatedAt = any()) }
                }
            }
        }

        Given("기기 저장이 실패한다") {
            val throwable = IllegalStateException(Arb.string().next())
            val context = RoutineRecordContext()
            coEvery { context.recordRepository.upsert(account = any(), record = any()) } throws throwable
            coEvery { context.recordRepository.updateDeleted(account = any(), recordId = any(), isDeleted = any(), updatedAt = any()) } throws throwable

            When("기록을 넣고 뺀다") {
                val addResult = context.add(date = context.today, amount = 1)
                val deleteResult = context.deleteUseCase(parameter = Arb.kotlinUuid().next())

                Then("TC-34479437-32c2-4d78-84ce-d20748c95e38 성공으로 다루지 않고 동기화를 요청하지 않는다") {
                    addResult.shouldBeFailure() shouldBeSameInstanceAs throwable
                    deleteResult.shouldBeFailure() shouldBeSameInstanceAs throwable
                    coVerify(exactly = 0) { context.requestSyncUseCase(parameter = any()) }
                }
            }
        }

        Given("기기 저장은 성공하고 서버 반영을 요청하는 동기화가 실패한다") {
            val context = RoutineRecordContext()
            coEvery { context.requestSyncUseCase(parameter = any()) } returns Result.failure(IllegalStateException(Arb.string().next()))

            When("기록을 넣고 뺀다") {
                val addResult = context.add(date = context.today, amount = 1)
                val deleteResult = context.deleteUseCase(parameter = Arb.kotlinUuid().next())

                Then("TC-0e6f404f-0e42-4df9-a75a-f94cc5c71169 기기에 저장한 결과로 성공을 전달한다") {
                    addResult.shouldBeSuccess() shouldBe context.savedList.single().id
                    deleteResult.shouldBeSuccess() shouldBe 1
                }
            }
        }

        Given("현재 계정의 루틴 기록 페이지가 준비되어 있다") {
            val account = Arb.accountUser().next()
            val routineId = Arb.kotlinUuid().next()
            val pagingData = PagingData.from(listOf(Arb.routineRecord(routineId = routineId).next()))
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.success(account))
            val repository = mockk<AccountRoutineRecordRepository>()
            every { repository.page(account = account, routineId = routineId) } returns flowOf(pagingData)
            val useCase = PageRoutineRecordUseCase(getAccountUseCase = getAccountUseCase, accountRoutineRecordRepository = repository)

            When("그 루틴의 기록 내역을 조회한다") {
                Then("현재 계정의 그 루틴 기록 페이지를 반환한다") {
                    useCase(parameter = routineId).test {
                        awaitItem().getOrThrow() shouldBeSameInstanceAs pagingData
                        awaitComplete()
                    }
                }
            }
        }

        Given("현재 계정 조회가 실패하도록 설정되어 있다") {
            val failure = IllegalStateException(Arb.string().next())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.failure(failure))
            val repository = mockk<AccountRoutineRecordRepository>(relaxed = true)
            val useCase = PageRoutineRecordUseCase(getAccountUseCase = getAccountUseCase, accountRoutineRecordRepository = repository)

            When("기록 내역을 조회한다") {
                Then("노출하는 기록 없이 계정 조회의 실패를 그대로 전달한다") {
                    useCase(parameter = Arb.kotlinUuid().next()).test {
                        awaitItem().shouldBeFailure { throwable -> throwable shouldBe failure }
                        awaitComplete()
                    }

                    verify(exactly = 0) { repository.page(account = any(), routineId = any()) }
                }
            }
        }
    })

private suspend fun RoutineRecordContext.add(
    date: LocalDate,
    amount: Int,
): Result<Uuid> = addUseCase(parameter = AddRoutineRecordUseCase.Parameter(routineId = checkNotNull(stored.value).id, date = date, amount = amount))
