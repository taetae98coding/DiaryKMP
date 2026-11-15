package io.github.taetae98coding.diary.domain.routine.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.routine.exception.RoutineNotFoundException
import io.github.taetae98coding.diary.domain.routine.exception.RoutineRecordAmountInvalidException
import io.github.taetae98coding.diary.domain.routine.exception.RoutineRecordDateOutOfRangeException
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coVerify
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.uuid.Uuid

class RoutineHomeQuickRoutineRecordUseCaseTest :
    BehaviorSpec({
        Given("한 번에 기록할 양이 250인 진행 중인 루틴이 목록에 표시된 뒤 저장된 양이 300으로 수정되었다") {
            val today = Arb.kotlinxLocalDate().next()
            val context = RoutineRecordContext(today = today, routine = inProgressRoutine(today = today, amountPerRecord = 250))
            context.stored.value = checkNotNull(context.stored.value).let { routine -> routine.copy(detail = routine.detail.copy(amountPerRecord = 300)) }

            When("빠른 기록을 넣는다") {
                val id = context.quick(amount = null).shouldBeSuccess()

                Then("TC-b09db714-23b1-45da-b55f-3509cd40a276 기록하는 시점에 저장된 한 번에 기록할 양으로 오늘 날짜의 기록을 만든다") {
                    val saved = context.savedList.single()
                    saved.id shouldBe id
                    saved.routineId shouldBe checkNotNull(context.stored.value).id
                    saved.date shouldBe today
                    saved.amount shouldBe 300
                    saved.isDeleted shouldBe false
                    coVerify(exactly = 1) { context.recordRepository.upsert(account = context.account, record = any()) }
                }

                Then("TC-d513ab4c-84b3-4ea4-8309-6d39e08fc80e 저장한 뒤 동기화를 한 번 요청한다") {
                    coVerify(exactly = 1) { context.requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) }
                }
            }
        }

        Given("진행 중인 루틴이 저장되어 있다") {
            val context = RoutineRecordContext()

            When("빠른 기록과 양 500을 입력한 기록을 하나씩 넣는다") {
                context.quick(amount = null).shouldBeSuccess()
                context.quick(amount = 500).shouldBeSuccess()

                Then("TC-eef32c86-88c5-4e8d-a914-f853d3d832ed 루틴의 정보는 저장하지 않는다") {
                    context.verifyRoutineUntouched()
                }

                Then("TC-851c367c-ac29-47f2-ba80-30a3faae119d TC-d513ab4c-84b3-4ea4-8309-6d39e08fc80e 기록마다 서로 다른 새 기록으로 저장한다") {
                    context.savedList.map { record -> record.amount } shouldBe listOf(checkNotNull(context.stored.value).detail.amountPerRecord, 500)
                    context.savedList.map { record -> record.date } shouldBe listOf(context.today, context.today)
                    context.savedList
                        .map { record -> record.id }
                        .distinct()
                        .size shouldBe 2
                }
            }

            When("양 0을 입력한 기록을 넣는다") {
                val zeroContext = RoutineRecordContext()
                val result = zeroContext.quick(amount = 0)

                Then("기록하지 않고 양 미입력으로 거절한다") {
                    result.shouldBeFailure().shouldBeInstanceOf<RoutineRecordAmountInvalidException>()
                    zeroContext.verifyNothingSaved()
                }
            }
        }

        Given("진행 중인 루틴의 양 입력 창을 연 뒤 루틴이 바뀌었다") {
            When("그 루틴이 삭제된 뒤 확인한다") {
                val context = RoutineRecordContext()
                context.stored.value = checkNotNull(context.stored.value).copy(isDeleted = true)
                val result = context.quick(amount = 500)

                Then("TC-ea83a6c0-2edc-4171-8b04-a1d3846983b1 기록을 만들지 않는다") {
                    result.shouldBeFailure().shouldBeInstanceOf<RoutineNotFoundException>()
                    context.verifyNothingSaved()
                }
            }

            When("그 루틴이 완료된 뒤 확인한다") {
                val context = RoutineRecordContext()
                context.stored.value = checkNotNull(context.stored.value).copy(isFinished = true)
                val result = context.quick(amount = 500)

                Then("TC-23b3f4f7-13eb-4898-a4af-b704e6d69481 기록을 만들지 않는다") {
                    result.shouldBeFailure().shouldBeInstanceOf<RoutineNotFoundException>()
                    context.verifyNothingSaved()
                    context.verifyRoutineUntouched()
                }
            }

            When("그 루틴의 시작 날짜가 오늘보다 뒤로 수정된 뒤 확인한다") {
                val context = RoutineRecordContext()
                context.stored.value = checkNotNull(context.stored.value).let { routine -> routine.copy(detail = routine.detail.copy(startDate = context.today.plus(1, DateTimeUnit.DAY))) }
                val result = context.quick(amount = 500)

                Then("TC-ea83a6c0-2edc-4171-8b04-a1d3846983b1 TC-a1d370fb-a8c0-4aa6-b5dc-848d1d827ac4 기록을 만들지 않는다") {
                    result.shouldBeFailure().shouldBeInstanceOf<RoutineRecordDateOutOfRangeException>()
                    context.verifyNothingSaved()
                }
            }

            When("그 루틴이 기기에서 조회되지 않는다") {
                val context = RoutineRecordContext(routine = null)
                val result = context.addQuickUseCase(parameter = AddQuickRoutineRecordUseCase.Parameter(routineId = Arb.kotlinUuid().next(), today = context.today, amount = null))

                Then("기록을 만들지 않는다") {
                    result.shouldBeFailure().shouldBeInstanceOf<RoutineNotFoundException>()
                    context.verifyNothingSaved()
                }
            }
        }

        Given("현재 계정을 확인할 수 없다") {
            val throwable = IllegalStateException(Arb.string().next())
            val context = RoutineRecordContext(accountResult = Result.failure(throwable))

            When("빠른 기록을 넣는다") {
                val result = context.quick(amount = null)

                Then("TC-2b60a7e5-af87-4138-8500-bd361b035fa8 기록이 생기지 않고 실패를 전달한다") {
                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                    context.verifyNothingSaved()
                }
            }
        }

        Given("로그인하지 않아 게스트 계정이 확인된다") {
            val context = RoutineRecordContext(accountResult = Result.success(Account.Guest))

            When("빠른 기록을 넣는다") {
                context.quick(amount = null).shouldBeSuccess()

                Then("TC-b2068d06-812b-4553-a5c3-a6cb17f5b483 게스트 계정 기준으로 기기에 저장한다") {
                    coVerify(exactly = 1) { context.recordRepository.upsert(account = Account.Guest, record = any()) }
                }
            }
        }
    })

private suspend fun RoutineRecordContext.quick(amount: Int?): Result<Uuid> = addQuickUseCase(parameter = AddQuickRoutineRecordUseCase.Parameter(routineId = stored.value?.id ?: Arb.kotlinUuid().next(), today = today, amount = amount))
