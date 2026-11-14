package io.github.taetae98coding.diary.domain.dday.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.model.dday.DDayDetail
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.dday.dday
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.dday.DDayInput
import io.github.taetae98coding.diary.domain.dday.ddayAddInput
import io.github.taetae98coding.diary.domain.dday.exception.DDayRepeatBlankException
import io.github.taetae98coding.diary.domain.dday.repeat.DDayRepeatInput
import io.github.taetae98coding.diary.domain.dday.repeat.DDayRepeatUnit
import io.github.taetae98coding.diary.domain.dday.repeat.toRepeatSetOrNull
import io.github.taetae98coding.diary.domain.dday.repository.AccountDDayRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.DateTimeUnit
import kotlin.time.Clock
import kotlin.time.Instant

class DDayDetailDDayUseCaseTest :
    BehaviorSpec({
        Given("현재 시각이 고정되어 있고 로그인한 계정의 디데이가 저장되어 있다") {
            listOf(Arb.kotlinxLocalDate().next(), null).forEach { endDate ->
                When("제목 앞뒤 공백, 줄바꿈 설명과 종료일 ${endDate ?: "지움"}으로 수정한다") {
                    val context = DetailContext()
                    val input = Arb.ddayAddInput().next().copy(title = " 결혼 ", description = "우리\n둘", endDate = endDate)
                    val result = context.updateUseCase(parameter = UpdateDDayUseCase.Parameter(id = context.stored.id, input = input))

                    Then("TC-1bb084c7-047c-40cc-9d22-ca00c1e3843e 입력한 그대로의 정보와 고정 시각으로 저장한다") {
                        result.shouldBeSuccess(1)
                        coVerify(exactly = 1) {
                            context.repository.updateDetail(
                                account = checkNotNull(context.account),
                                ddayId = context.stored.id,
                                detail =
                                    DDayDetail(
                                        title = " 결혼 ",
                                        description = "우리\n둘",
                                        color = input.color,
                                        baseDate = input.baseDate,
                                        countsBaseDay = input.countsBaseDay,
                                        repeats = context.storedRepeatsOf(input),
                                        endDate = endDate,
                                        overflowPolicy = input.overflowPolicy,
                                    ),
                                updatedAt = context.now,
                            )
                        }
                    }

                    Then("TC-36c2fd08-bb27-435f-92e0-cd08e282d8a3 정보만 바꾸고 삭제 여부와 다른 저장은 요청하지 않는다") {
                        coVerify(exactly = 0) { context.repository.updateDeleted(account = any(), ddayId = any(), isDeleted = any(), updatedAt = any()) }
                        coVerify(exactly = 0) { context.repository.upsert(account = any(), dday = any(), tagIdSet = any()) }
                    }

                    Then("TC-541e8b7e-a72b-4768-ae1e-8a46e49afb9c TC-439ff23c-cd9a-42eb-9348-dcbfb47a7fc4 저장한 뒤 동기화를 한 번 요청한다") {
                        coVerifyOrder {
                            context.repository.updateDetail(account = any(), ddayId = any(), detail = any(), updatedAt = any())
                            context.requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                        }
                        coVerify(exactly = 1) { context.requestSyncUseCase(parameter = any()) }
                    }
                }
            }

            listOf(
                listOf(DDayRepeatInput(2, DDayRepeatUnit.YEAR)) to setOf<DateTimeUnit.DateBased>(DateTimeUnit.MonthBased(months = 24)),
                listOf(DDayRepeatInput(100, DDayRepeatUnit.DAY), DDayRepeatInput(1, DDayRepeatUnit.YEAR), DDayRepeatInput(3, DDayRepeatUnit.MONTH)) to
                    setOf(DateTimeUnit.DayBased(days = 100), DateTimeUnit.MonthBased(months = 12), DateTimeUnit.MonthBased(months = 3)),
                listOf(DDayRepeatInput("0050".toInt(), DDayRepeatUnit.DAY)) to setOf(DateTimeUnit.DayBased(days = 50)),
                listOf(
                    DDayRepeatInput(1, DDayRepeatUnit.YEAR),
                    DDayRepeatInput(12, DDayRepeatUnit.MONTH),
                    DDayRepeatInput(100, DDayRepeatUnit.DAY),
                    DDayRepeatInput(100, DDayRepeatUnit.DAY),
                ) to setOf(DateTimeUnit.MonthBased(months = 12), DateTimeUnit.DayBased(days = 100)),
                emptyList<DDayRepeatInput>() to emptySet(),
            ).forEach { (repeatList, record) ->
                When("반복 간격을 $repeatList 로 고쳐 수정한다") {
                    val context = DetailContext()
                    val input = Arb.ddayAddInput().next().copy(repeatList = repeatList)
                    context.updateUseCase(parameter = UpdateDDayUseCase.Parameter(id = context.stored.id, input = input)).shouldBeSuccess()

                    Then("TC-bc7c4599-5f11-4d6e-92f4-ceb456234b4c 저장된 간격 모음을 입력한 간격 모음으로 바꾼다") {
                        coVerify(exactly = 1) {
                            context.repository.updateDetail(account = any(), ddayId = context.stored.id, detail = match { detail -> detail.repeats == record }, updatedAt = any())
                        }
                    }
                }
            }

            listOf("", "   ").forEach { title ->
                When("제목을 '$title'로 비운 채 수정한다") {
                    val context = DetailContext()
                    val input = Arb.ddayAddInput().next().copy(title = title)
                    context.updateUseCase(parameter = UpdateDDayUseCase.Parameter(id = context.stored.id, input = input)).shouldBeSuccess()

                    Then("TC-cbe821d7-7fa6-48cb-91b1-8847d83889e4 기존 제목을 유지하고 나머지 정보만 반영한다") {
                        coVerify(exactly = 1) {
                            context.repository.updateDetail(
                                account = any(),
                                ddayId = context.stored.id,
                                detail = match { detail -> detail.title == context.stored.detail.title && detail.description == input.description && detail.baseDate == input.baseDate },
                                updatedAt = any(),
                            )
                        }
                    }
                }
            }

            listOf(
                "제목을 입력하고" to Arb.nonBlankString().next(),
                "제목을 비우고" to "",
                "제목을 공백만으로 두고" to "   ",
            ).forEach { (name, title) ->
                When("$name 숫자가 비어 있는 간격이 있는 채 수정한다") {
                    val context = DetailContext()
                    val input = Arb.ddayAddInput().next().copy(title = title, repeatList = listOf(DDayRepeatInput(null, DDayRepeatUnit.YEAR)))
                    val result = context.updateUseCase(parameter = UpdateDDayUseCase.Parameter(id = context.stored.id, input = input))

                    Then("TC-55d21aa4-9b96-4c9b-be03-ffc5326d5277 아무 내용도 수정하지 않고 반복 간격 미입력으로 거절한다") {
                        result.shouldBeFailure().shouldBeInstanceOf<DDayRepeatBlankException>()
                        coVerify(exactly = 0) { context.repository.updateDetail(account = any(), ddayId = any(), detail = any(), updatedAt = any()) }
                        coVerify(exactly = 0) { context.requestSyncUseCase(parameter = any()) }
                    }
                }
            }

            When("디데이를 삭제한다") {
                val context = DetailContext()
                val result = context.deleteUseCase(parameter = context.stored.id)

                Then("TC-c802b994-26d9-4e7f-ac93-7feea6e64b39 삭제 여부와 고정 시각만 저장한다") {
                    result.shouldBeSuccess(1)
                    coVerify(exactly = 1) {
                        context.repository.updateDeleted(account = checkNotNull(context.account), ddayId = context.stored.id, isDeleted = true, updatedAt = context.now)
                    }
                    coVerify(exactly = 0) { context.repository.updateDetail(account = any(), ddayId = any(), detail = any(), updatedAt = any()) }
                }

                Then("TC-541e8b7e-a72b-4768-ae1e-8a46e49afb9c TC-439ff23c-cd9a-42eb-9348-dcbfb47a7fc4 삭제를 저장한 뒤 동기화를 한 번 요청한다") {
                    coVerifyOrder {
                        context.repository.updateDeleted(account = any(), ddayId = any(), isDeleted = any(), updatedAt = any())
                        context.requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                    }
                    coVerify(exactly = 1) { context.requestSyncUseCase(parameter = any()) }
                }
            }
        }

        Given("로그인하지 않아 게스트 계정이 확인된다") {
            val context = DetailContext(accountResult = Result.success(Account.Guest))

            When("수정과 삭제를 실행한다") {
                context.updateUseCase(parameter = UpdateDDayUseCase.Parameter(id = context.stored.id, input = Arb.ddayAddInput().next())).shouldBeSuccess()
                context.deleteUseCase(parameter = context.stored.id).shouldBeSuccess()

                Then("TC-c880c2ae-beb7-4d16-8d35-aceafa03dae2 게스트 계정 기준으로 기기에 저장한다") {
                    coVerify(exactly = 1) { context.repository.updateDetail(account = Account.Guest, ddayId = context.stored.id, detail = any(), updatedAt = any()) }
                    coVerify(exactly = 1) { context.repository.updateDeleted(account = Account.Guest, ddayId = context.stored.id, isDeleted = true, updatedAt = any()) }
                }
            }
        }

        Given("기기 저장이 실패한다") {
            val throwable = IllegalStateException(Arb.string().next())
            val context = DetailContext()
            coEvery { context.repository.updateDetail(account = any(), ddayId = any(), detail = any(), updatedAt = any()) } throws throwable
            coEvery { context.repository.updateDeleted(account = any(), ddayId = any(), isDeleted = any(), updatedAt = any()) } throws throwable

            When("수정과 삭제를 실행한다") {
                val updateResult = context.updateUseCase(parameter = UpdateDDayUseCase.Parameter(id = context.stored.id, input = Arb.ddayAddInput().next()))
                val deleteResult = context.deleteUseCase(parameter = context.stored.id)

                Then("TC-e16a87ea-a3e4-4cbe-9fce-6091637d1f84 성공으로 다루지 않고 동기화를 요청하지 않는다") {
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
                val updateResult = context.updateUseCase(parameter = UpdateDDayUseCase.Parameter(id = context.stored.id, input = Arb.ddayAddInput().next()))
                val deleteResult = context.deleteUseCase(parameter = context.stored.id)

                Then("TC-7174af3d-700c-4233-9d55-dc036e0677dc 기기에 저장한 결과로 성공을 전달한다") {
                    updateResult.shouldBeSuccess(1)
                    deleteResult.shouldBeSuccess(1)
                }
            }
        }

        Given("상세 대상 디데이를 조회한다") {
            When("현재 계정을 확인할 수 없다") {
                val throwable = IllegalStateException(Arb.string().next())
                val context = DetailContext(accountResult = Result.failure(throwable))

                Then("TC-e8e52e4a-5369-4c79-abe4-1617bc254ab2 계정 확인 실패를 전달한다") {
                    context.findUseCase(parameter = context.stored.id).first().shouldBeFailure() shouldBeSameInstanceAs throwable
                }
            }

            When("디데이가 다른 계정과만 연결되어 있다") {
                val context = DetailContext(find = flowOf(null))

                Then("TC-e8e52e4a-5369-4c79-abe4-1617bc254ab2 대상 디데이가 없다") {
                    context.findUseCase(parameter = context.stored.id).first().shouldBeSuccess(null)
                    coVerify(exactly = 1) { context.repository.find(account = checkNotNull(context.account), ddayId = context.stored.id) }
                }
            }

            When("디데이가 현재 계정과 연결되어 있다") {
                val context = DetailContext()

                Then("TC-e8e52e4a-5369-4c79-abe4-1617bc254ab2 그 디데이의 정보와 반복 간격이 함께 조회된다") {
                    context.findUseCase(parameter = context.stored.id).first().shouldBeSuccess(context.stored)
                }
            }

            When("현재 계정의 디데이가 삭제 상태다") {
                val deleted = Arb.dday(isDeleted = true).next()
                val context = DetailContext(stored = deleted)

                Then("TC-c4c2466a-deac-4b32-892a-619e43947493 삭제 상태인 디데이도 조회한다") {
                    context.findUseCase(parameter = deleted.id).first().shouldBeSuccess(deleted)
                }
            }
        }
    })

private class DetailContext(
    accountResult: Result<Account> = Result.success(Arb.accountUser().next()),
    val stored: DDay = Arb.dday(isDeleted = false).next(),
    find: Flow<DDay?> = flowOf(stored),
) {
    val account: Account? = accountResult.getOrNull()
    val now: Instant = Arb.secondPrecisionInstant().next()

    val repository: AccountDDayRepository =
        mockk<AccountDDayRepository>().also { repository ->
            every { repository.find(account = any(), ddayId = any()) } returns find
            coEvery { repository.updateDetail(account = any(), ddayId = any(), detail = any(), updatedAt = any()) } returns 1
            coEvery { repository.updateDeleted(account = any(), ddayId = any(), isDeleted = any(), updatedAt = any()) } returns 1
        }

    val requestSyncUseCase: RequestSyncUseCase =
        mockk<RequestSyncUseCase>().also { useCase ->
            coEvery { useCase(parameter = any()) } returns Result.success(Unit)
        }

    private val getAccountUseCase: GetAccountUseCase =
        mockk<GetAccountUseCase>().also { useCase ->
            every { useCase() } returns flowOf(accountResult)
        }

    private val clock: Clock = mockk<Clock>().also { clock -> every { clock.now() } returns now }

    val findUseCase: FindDDayUseCase = FindDDayUseCase(getAccountUseCase = getAccountUseCase, accountDDayRepository = repository)

    val updateUseCase: UpdateDDayUseCase =
        UpdateDDayUseCase(
            getAccountUseCase = getAccountUseCase,
            requestSyncUseCase = requestSyncUseCase,
            findDDayUseCase = findUseCase,
            accountDDayRepository = repository,
            clock = clock,
        )

    val deleteUseCase: DeleteDDayUseCase =
        DeleteDDayUseCase(
            getAccountUseCase = getAccountUseCase,
            requestSyncUseCase = requestSyncUseCase,
            accountDDayRepository = repository,
            clock = clock,
        )

    fun storedRepeatsOf(input: DDayInput): Set<DateTimeUnit.DateBased> = checkNotNull(input.repeatList.toRepeatSetOrNull())
}
