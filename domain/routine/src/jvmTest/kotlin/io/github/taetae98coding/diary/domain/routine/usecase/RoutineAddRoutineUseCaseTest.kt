package io.github.taetae98coding.diary.domain.routine.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.routine.Routine
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_AMOUNT_PER_RECORD
import io.github.taetae98coding.diary.domain.routine.ROUTINE_DEFAULT_CYCLE
import io.github.taetae98coding.diary.domain.routine.RoutineInput
import io.github.taetae98coding.diary.domain.routine.exception.RoutineAmountPerRecordInvalidException
import io.github.taetae98coding.diary.domain.routine.exception.RoutineGoalAmountInvalidException
import io.github.taetae98coding.diary.domain.routine.exception.RoutineTitleBlankException
import io.github.taetae98coding.diary.domain.routine.exception.RoutineUnitInvalidException
import io.github.taetae98coding.diary.domain.routine.repository.AccountRoutineRepository
import io.github.taetae98coding.diary.domain.routine.routineInput
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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

class RoutineAddRoutineUseCaseTest :
    BehaviorSpec({
        Given("제목이 비어 있거나 공백뿐이고 목표량은 없으며 한 번에 기록할 양은 1이다") {
            listOf("", "   ").forEach { title ->
                When("제목 '$title'로 루틴 추가를 요청한다") {
                    val context = AddContext()
                    val result = context.useCase(parameter = parameter(input = Arb.routineInput().next().copy(title = title, goalAmount = null, amountPerRecord = 1)))

                    Then("TC-40d5f03e-cc2a-4218-862e-0a65e18a1dbe 저장하지 않고 제목 미입력으로 거절한다") {
                        result.shouldBeFailure().shouldBeInstanceOf<RoutineTitleBlankException>()
                        context.verifyNothingSaved()
                    }
                }
            }
        }

        Given("제목과 한 번에 기록할 양 250이 있고 목표량이 0이다") {
            When("루틴 추가를 요청한다") {
                val context = AddContext()
                val result = context.useCase(parameter = parameter(input = Arb.routineInput().next().copy(goalAmount = 0, amountPerRecord = 250)))

                Then("TC-a5df98f4-a556-4440-8bbd-6c48adcd94c5 목표량이 없는 루틴으로 바꾸지 않고 목표량 미입력으로 거절한다") {
                    result.shouldBeFailure().shouldBeInstanceOf<RoutineGoalAmountInvalidException>()
                    context.verifyNothingSaved()
                }
            }
        }

        Given("제목과 목표량 2000이 있고 한 번에 기록할 양이 0이다") {
            When("루틴 추가를 요청한다") {
                val context = AddContext()
                val result = context.useCase(parameter = parameter(input = Arb.routineInput().next().copy(goalAmount = 2000, amountPerRecord = 0)))

                Then("TC-64877ba3-73b4-4039-a063-7a9a729d9a5c 기본값으로 바꾸지 않고 한 번에 기록할 양 미입력으로 거절한다") {
                    result.shouldBeFailure().shouldBeInstanceOf<RoutineAmountPerRecordInvalidException>()
                    context.verifyNothingSaved()
                }
            }
        }

        Given("여러 조건이 함께 성립하지 않는다") {
            When("제목이 비어 있고 목표량과 한 번에 기록할 양이 0인 루틴 추가를 요청한다") {
                val context = AddContext()
                val result = context.useCase(parameter = parameter(input = Arb.routineInput().next().copy(title = "", goalAmount = 0, amountPerRecord = 0)))

                Then("TC-0464f8ad-8d7f-48ad-9383-97da3d4dbb0b 제목 미입력 하나로만 거절한다") {
                    result.shouldBeFailure().shouldBeInstanceOf<RoutineTitleBlankException>()
                    context.verifyNothingSaved()
                }
            }

            When("제목이 있고 목표량과 한 번에 기록할 양이 0인 루틴 추가를 요청한다") {
                val context = AddContext()
                val result = context.useCase(parameter = parameter(input = Arb.routineInput().next().copy(goalAmount = 0, amountPerRecord = 0)))

                Then("TC-0464f8ad-8d7f-48ad-9383-97da3d4dbb0b 목표량 미입력 하나로만 거절한다") {
                    result.shouldBeFailure().shouldBeInstanceOf<RoutineGoalAmountInvalidException>()
                    context.verifyNothingSaved()
                }
            }
        }

        Given("시작 날짜가 2026-11-10이고 매일, 회, 목표량 없음, 한 번에 기록할 양 1인 입력에서 값 하나만 바꿨다") {
            val base =
                Arb.routineInput().next().copy(
                    startDate = LocalDate(2026, 11, 10),
                    cycle = RoutineCycle.DAILY,
                    unit = "회",
                    goalAmount = null,
                    amountPerRecord = 1,
                )

            // 다섯 값이 아닌 주기는 입력의 타입으로 만들 수 없어, 저장 값에서 읽지 못하는 것을 저장소 테스트가 확인한다.
            // 앞뒤에 공백이 있는 단위는 추가 요청이 공백을 빼고 기록하므로, 그 단위가 성립하지 않는 것은 RoutineUnitTest가 확인한다.
            listOf(
                base to true,
                base.copy(cycle = RoutineCycle.WEEKDAYS) to true,
                base.copy(cycle = RoutineCycle.WEEKENDS) to true,
                base.copy(cycle = RoutineCycle.WEEKLY) to true,
                base.copy(cycle = RoutineCycle.MONTHLY) to true,
                base.copy(unit = "ml") to true,
                base.copy(unit = "L") to true,
                base.copy(unit = "ML") to true,
                base.copy(unit = "분 걷기") to true,
                base.copy(unit = "") to true,
                base.copy(unit = "가") to true,
                base.copy(unit = "kilometers") to true,
                base.copy(unit = "가나다라마바사아자차") to true,
                base.copy(unit = "kilometerss") to false,
                base.copy(unit = "가나다라마바사아자차카") to false,
                base.copy(unit = "밀리\n리터") to false,
                base.copy(goalAmount = 0) to false,
                base.copy(goalAmount = 1) to true,
                base.copy(goalAmount = 9_999_999) to true,
                base.copy(goalAmount = 10_000_000) to false,
                base.copy(amountPerRecord = 0) to false,
                base.copy(amountPerRecord = 9_999_999) to true,
                base.copy(amountPerRecord = 10_000_000) to false,
            ).forEachIndexed { index, (input, isValid) ->
                When("$index: ${input.cycle}, 단위 길이 ${input.unit.length}, 목표량 ${input.goalAmount}, 한 번에 기록할 양 ${input.amountPerRecord}로 추가를 요청한다") {
                    val context = AddContext()
                    val result = context.useCase(parameter = parameter(input = input))

                    Then("TC-e2271ac7-d2fb-49ad-929e-91ab22b83107 유효 범위를 모두 만족할 때만 루틴이 저장된다") {
                        result.isSuccess shouldBe isValid
                        context.savedList.size shouldBe if (isValid) 1 else 0
                    }
                }
            }
        }

        Given("단위가 10자를 넘거나 글자 사이에 줄바꿈이 있다") {
            listOf("kilometerss", "가나다라마바사아자차카", "밀리\n리터", "밀리\r\n리터").forEachIndexed { index, unit ->
                When("$index: 그 단위로 루틴 추가를 요청한다") {
                    val context = AddContext()
                    val result = context.useCase(parameter = parameter(input = Arb.routineInput().next().copy(unit = unit)))

                    Then("TC-9906df56-12e2-4162-b857-8ddcb290493d 앞 10자로 자르거나 줄바꿈을 빼지 않고 단위 오류로 거절한다") {
                        result.shouldBeFailure().shouldBeInstanceOf<RoutineUnitInvalidException>()
                        context.verifyNothingSaved()
                    }
                }
            }
        }

        Given("제목 운동만 있고 설명, 단위와 목표량이 비어 있으며 고른 태그가 없다") {
            val context = AddContext()
            val input =
                RoutineInput(
                    title = "운동",
                    description = "",
                    color = Arb.long().next(),
                    startDate = LocalDate(2026, 3, 1),
                    cycle = ROUTINE_DEFAULT_CYCLE,
                    unit = "",
                    goalAmount = null,
                    amountPerRecord = ROUTINE_DEFAULT_AMOUNT_PER_RECORD,
                )

            When("루틴 추가를 요청한다") {
                context.useCase(parameter = parameter(input = input)).shouldBeSuccess()

                Then("TC-2438f374-f8f7-4174-9a1c-065b002ce8bc 빈 설명, 빈 단위, 목표량 없음으로 저장한다") {
                    val saved = context.savedList.single().detail
                    saved.title shouldBe "운동"
                    saved.description shouldBe ""
                    saved.unit shouldBe ""
                    saved.goalAmount shouldBe null
                    saved.cycle shouldBe RoutineCycle.DAILY
                    saved.amountPerRecord shouldBe 1
                }

                Then("TC-e0d1f1aa-a05a-47ba-98f3-b09ea206f9f5 태그 연결 없이 저장한다") {
                    coVerify(exactly = 1) { context.repository.upsert(account = any(), routine = any(), tagIdSet = emptySet()) }
                }
            }
        }

        Given("같은 내용의 루틴 추가를 두 번 요청한다") {
            val context = AddContext()
            val parameter = parameter(input = Arb.routineInput().next())

            When("두 번 모두 저장된다") {
                val firstId = context.useCase(parameter = parameter).shouldBeSuccess()
                val secondId = context.useCase(parameter = parameter).shouldBeSuccess()

                Then("TC-ca07dfdd-008f-41f1-8580-59b752caba54 서로 다른 식별 정보의 새 루틴을 각각 저장한다") {
                    firstId shouldNotBe secondId
                    context.savedList.map { routine -> routine.id } shouldBe listOf(firstId, secondId)
                    context.savedList
                        .map { routine -> routine.detail }
                        .distinct()
                        .size shouldBe 1
                }
            }
        }

        Given("앞뒤 공백이 있는 제목과 줄바꿈이 있는 설명을 입력했다") {
            listOf(LocalDate(2026, 3, 20), LocalDate(2020, 5, 1), LocalDate(2030, 12, 31)).forEach { startDate ->
                When("시작 날짜 ${startDate}로 루틴 추가를 요청한다") {
                    val context = AddContext()
                    val color = Arb.long().next()
                    val input =
                        Arb.routineInput().next().copy(
                            title = " 물 마시기 ",
                            description = "하루\n2L",
                            color = color,
                            startDate = startDate,
                            unit = "ml",
                        )
                    context.useCase(parameter = parameter(input = input)).shouldBeSuccess()

                    Then("TC-741671a6-e78c-45a2-a5ed-37e691694d54 입력한 그대로 저장한다") {
                        val saved = context.savedList.single().detail
                        saved.title shouldBe " 물 마시기 "
                        saved.description shouldBe "하루\n2L"
                        saved.color shouldBe color
                        saved.startDate shouldBe startDate
                        saved.unit shouldBe "ml"
                    }

                    Then("TC-91b612ad-1cb5-467b-91df-2f6b09f16d46 TC-43eabd90-fd95-4044-b9a6-bcc229ee7ae9 완료되지 않은 상태로 저장한다") {
                        context.savedList.single().isFinished shouldBe false
                    }
                }
            }
        }

        Given("제목 물 마시기가 있고 주기를 골랐다") {
            RoutineCycle.entries.forEach { cycle ->
                When("${cycle}로 루틴 추가를 요청한다") {
                    val context = AddContext()
                    context.useCase(parameter = parameter(input = Arb.routineInput().next().copy(title = "물 마시기", cycle = cycle))).shouldBeSuccess()

                    Then("TC-f2be58a1-b7fe-4a40-bad1-6ba153b6ea52 고른 주기로 저장한다") {
                        context.savedList
                            .single()
                            .detail.cycle shouldBe cycle
                    }
                }
            }
        }

        Given("제목 물 마시기가 있고 단위를 입력했다") {
            listOf(
                "ml" to "ml",
                " ml" to "ml",
                "ml " to "ml",
                "  ml  " to "ml",
                "분 걷기" to "분 걷기",
                "ML" to "ML",
                "kilometers" to "kilometers",
                "" to "",
                "   " to "",
            ).forEachIndexed { index, (unit, expected) ->
                When("$index: 단위 '$unit'로 루틴 추가를 요청한다") {
                    val context = AddContext()
                    context.useCase(parameter = parameter(input = Arb.routineInput().next().copy(title = "물 마시기", unit = unit))).shouldBeSuccess()

                    Then("TC-74cbf590-0f9e-473b-8bcb-9be297195b61 앞뒤 공백을 뺀 글자로 기록한다") {
                        context.savedList
                            .single()
                            .detail.unit shouldBe expected
                    }
                }
            }
        }

        Given("앞뒤 공백을 포함하면 10자를 넘지만 공백을 빼면 10자인 단위를 입력했다") {
            When("루틴 추가를 요청한다") {
                val context = AddContext()
                context.useCase(parameter = parameter(input = Arb.routineInput().next().copy(unit = "  kilometers  "))).shouldBeSuccess()

                Then("TC-303d5da6-b7a9-4de1-9642-a37ed2f38262 공백을 뺀 10자로 저장한다") {
                    context.savedList
                        .single()
                        .detail.unit shouldBe "kilometers"
                }
            }
        }

        Given("목표량과 한 번에 기록할 양을 읽은 수가 있다") {
            listOf(
                2000 to 250,
                500 to 1,
                null to 1,
                1 to 9_999_999,
                9_999_999 to 1,
            ).forEach { (goalAmount, amountPerRecord) ->
                When("목표량 $goalAmount, 한 번에 기록할 양 ${amountPerRecord}로 루틴 추가를 요청한다") {
                    val context = AddContext()
                    val input = Arb.routineInput().next().copy(goalAmount = goalAmount, amountPerRecord = amountPerRecord)
                    context.useCase(parameter = parameter(input = input)).shouldBeSuccess()

                    Then("TC-44c3359c-ff23-4536-ab05-668dc12e6772 읽은 수로 저장한다") {
                        val saved = context.savedList.single().detail
                        saved.goalAmount shouldBe goalAmount
                        saved.amountPerRecord shouldBe amountPerRecord
                    }
                }
            }
        }

        Given("현재 시각이 고정되어 있고 현재 계정이 확인된다") {
            val context = AddContext()
            val input = Arb.routineInput().next()
            val tagIdSet = setOf(Arb.kotlinUuid().next(), Arb.kotlinUuid().next())

            When("루틴 추가를 요청한다") {
                val id = context.useCase(parameter = AddRoutineUseCase.Parameter(input = input, tagIdSet = tagIdSet)).shouldBeSuccess()

                Then("TC-35e6643c-83f3-4529-bc63-60b9ecff05fe 미삭제 상태로 생성·수정 시각이 모두 현재 시각이다") {
                    val saved = context.savedList.single()
                    saved.id shouldBe id
                    saved.isDeleted shouldBe false
                    saved.createdAt shouldBe context.now
                    saved.updatedAt shouldBe context.now
                }

                Then("TC-cb549cbe-7bb3-4087-9871-d262ead82419 TC-721ea497-efd4-4db1-b751-a7204dc77179 현재 계정과 고른 태그와 함께 한 번에 저장한다") {
                    coVerify(exactly = 1) { context.repository.upsert(account = checkNotNull(context.account), routine = any(), tagIdSet = tagIdSet) }
                }

                Then("TC-36dfb227-5088-4d09-b747-05cfb52f54d5 기기에 저장한 뒤 동기화를 한 번 요청한다") {
                    coVerifyOrder {
                        context.repository.upsert(account = any(), routine = any(), tagIdSet = any())
                        context.requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                    }
                    coVerify(exactly = 1) { context.requestSyncUseCase(parameter = any()) }
                }

                Then("입력한 내용을 루틴 정보로 저장한다") {
                    val saved = context.savedList.single().detail
                    saved.title shouldBe input.title
                    saved.description shouldBe input.description
                    saved.color shouldBe input.color
                    saved.startDate shouldBe input.startDate
                    saved.cycle shouldBe input.cycle
                    saved.unit shouldBe input.unit
                    saved.goalAmount shouldBe input.goalAmount
                    saved.amountPerRecord shouldBe input.amountPerRecord
                }
            }
        }

        Given("현재 계정 조회가 실패한다") {
            val throwable = IllegalStateException(Arb.string().next())
            val context = AddContext(accountResult = Result.failure(throwable))

            When("루틴 추가를 요청한다") {
                val result = context.useCase(parameter = parameter(input = Arb.routineInput().next()))

                Then("TC-4dc79494-e19e-4913-adcb-707c74606748 저장하지 않고 실패를 전달한다") {
                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                    context.verifyNothingSaved()
                }
            }
        }

        Given("로그인하지 않아 게스트 계정이 확인된다") {
            val context = AddContext(accountResult = Result.success(Account.Guest))

            When("루틴 추가를 요청한다") {
                context.useCase(parameter = parameter(input = Arb.routineInput().next())).shouldBeSuccess()

                Then("TC-7ad79a89-3a76-4336-9674-901ee9ce9d57 게스트 계정 기준으로 기기에 저장한다") {
                    coVerify(exactly = 1) { context.repository.upsert(account = Account.Guest, routine = any(), tagIdSet = any()) }
                }
            }
        }

        Given("기기 저장이 실패한다") {
            val throwable = IllegalStateException(Arb.string().next())
            val context = AddContext()
            coEvery { context.repository.upsert(account = any(), routine = any(), tagIdSet = any()) } throws throwable

            When("루틴 추가를 요청한다") {
                val result = context.useCase(parameter = parameter(input = Arb.routineInput().next(), tagIdSet = setOf(Arb.kotlinUuid().next())))

                Then("TC-2bb300e0-0d65-4b6a-8660-4ea88218e211 추가를 성공으로 다루지 않고 동기화를 요청하지 않는다") {
                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                    coVerify(exactly = 0) { context.requestSyncUseCase(parameter = any()) }
                }
            }
        }

        Given("기기 저장은 성공하고 서버 반영을 요청하는 동기화가 실패한다") {
            val context = AddContext()
            coEvery { context.requestSyncUseCase(parameter = any()) } returns Result.failure(IllegalStateException(Arb.string().next()))

            When("루틴 추가를 요청한다") {
                val result = context.useCase(parameter = parameter(input = Arb.routineInput().next()))

                Then("TC-00d23677-06fd-4541-bde3-7288b7928e7e 기기에 저장한 결과로 성공을 전달한다") {
                    result.shouldBeSuccess() shouldBe context.savedList.single().id
                }
            }
        }
    })

private fun parameter(
    input: RoutineInput,
    tagIdSet: Set<Uuid> = emptySet(),
): AddRoutineUseCase.Parameter = AddRoutineUseCase.Parameter(input = input, tagIdSet = tagIdSet)

private class AddContext(
    accountResult: Result<Account> = Result.success(Arb.accountUser().next()),
) {
    val account: Account? = accountResult.getOrNull()
    val now: Instant = Arb.secondPrecisionInstant().next()
    val savedList: MutableList<Routine> = mutableListOf()

    val repository: AccountRoutineRepository =
        mockk<AccountRoutineRepository>().also { repository ->
            coEvery { repository.upsert(account = any(), routine = any(), tagIdSet = any()) } answers { savedList += secondArg<Routine>() }
        }

    val requestSyncUseCase: RequestSyncUseCase =
        mockk<RequestSyncUseCase>().also { useCase ->
            coEvery { useCase(parameter = any()) } returns Result.success(Unit)
        }

    val useCase: AddRoutineUseCase =
        AddRoutineUseCase(
            getAccountUseCase =
                mockk<GetAccountUseCase>().also { useCase ->
                    every { useCase() } returns flowOf(accountResult)
                },
            requestSyncUseCase = requestSyncUseCase,
            accountRoutineRepository = repository,
            clock =
                mockk<Clock>().also { clock ->
                    every { clock.now() } returns now
                },
        )

    fun verifyNothingSaved() {
        coVerify(exactly = 0) { repository.upsert(account = any(), routine = any(), tagIdSet = any()) }
        coVerify(exactly = 0) { requestSyncUseCase(parameter = any()) }
    }
}
