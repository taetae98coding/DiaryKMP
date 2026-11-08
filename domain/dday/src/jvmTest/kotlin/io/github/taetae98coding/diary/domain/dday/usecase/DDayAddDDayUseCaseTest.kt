package io.github.taetae98coding.diary.domain.dday.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.dday.DDay
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.dday.ddayAddInput
import io.github.taetae98coding.diary.domain.dday.exception.DDayRepeatBlankException
import io.github.taetae98coding.diary.domain.dday.exception.DDayTitleBlankException
import io.github.taetae98coding.diary.domain.dday.repeat.DDayRepeatInput
import io.github.taetae98coding.diary.domain.dday.repeat.DDayRepeatUnit
import io.github.taetae98coding.diary.domain.dday.repeat.toRepeatSetOrNull
import io.github.taetae98coding.diary.domain.dday.repository.AccountDDayRepository
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

class DDayAddDDayUseCaseTest :
    BehaviorSpec({
        Given("제목이 비어 있거나 공백뿐이다") {
            listOf("", "   ").forEach { title ->
                When("제목 '$title'로 디데이 추가를 요청한다") {
                    val context = AddContext()
                    val result = context.useCase(parameter = parameter(input = Arb.ddayAddInput().next().copy(title = title)))

                    Then("TC-4438b530-b4f4-4678-b57d-852e6020fa11 저장하지 않고 제목 미입력으로 거절한다") {
                        result.shouldBeFailure().shouldBeInstanceOf<DDayTitleBlankException>()
                        context.verifyNothingSaved()
                    }
                }
            }
        }

        Given("제목이 있고 100일 간격과 숫자가 비어 있거나 0인 간격이 함께 있다") {
            listOf(
                DDayRepeatInput(number = null, unit = DDayRepeatUnit.DAY),
                DDayRepeatInput(number = 0, unit = DDayRepeatUnit.DAY),
                DDayRepeatInput(number = 0, unit = DDayRepeatUnit.MONTH),
                DDayRepeatInput(number = "000".toInt(), unit = DDayRepeatUnit.YEAR),
            ).forEach { blank ->
                When("$blank 와 함께 디데이 추가를 요청한다") {
                    val context = AddContext()
                    val input = Arb.ddayAddInput().next().copy(repeatList = listOf(DDayRepeatInput(number = 100, unit = DDayRepeatUnit.DAY), blank))
                    val result = context.useCase(parameter = parameter(input = input))

                    Then("TC-378b6576-59fe-42da-bf1a-dcb35472e91a 그 간격만 빼고 저장하지 않고 반복 간격 미입력으로 거절한다") {
                        result.shouldBeFailure().shouldBeInstanceOf<DDayRepeatBlankException>()
                        context.verifyNothingSaved()
                    }
                }
            }
        }

        Given("제목이 비어 있고 숫자가 0인 간격이 있다") {
            When("디데이 추가를 요청한다") {
                val context = AddContext()
                val input = Arb.ddayAddInput().next().copy(title = "", repeatList = listOf(DDayRepeatInput(number = 0, unit = DDayRepeatUnit.DAY)))
                val result = context.useCase(parameter = parameter(input = input))

                Then("TC-e81d3709-abf1-439a-9345-5dd833013cdc 제목 미입력 하나로만 거절한다") {
                    result.shouldBeFailure().shouldBeInstanceOf<DDayTitleBlankException>()
                    context.verifyNothingSaved()
                }
            }
        }

        Given("같은 내용의 디데이 추가를 두 번 요청한다") {
            val context = AddContext()
            val parameter = parameter(input = Arb.ddayAddInput().next())

            When("두 번 모두 저장된다") {
                val firstId = context.useCase(parameter = parameter).shouldBeSuccess()
                val secondId = context.useCase(parameter = parameter).shouldBeSuccess()

                Then("TC-63fbc1a6-ba35-434e-968d-68b1cc998697 서로 다른 식별 정보의 새 디데이를 각각 저장한다") {
                    firstId shouldNotBe secondId
                    context.savedList.map { dday -> dday.id } shouldBe listOf(firstId, secondId)
                    context.savedList
                        .map { dday -> dday.detail }
                        .distinct()
                        .size shouldBe 1
                }
            }
        }

        Given("현재 시각이 고정되어 있고 현재 계정이 확인된다") {
            val context = AddContext()
            val input = Arb.ddayAddInput().next()
            val tagIdSet = setOf(Arb.kotlinUuid().next(), Arb.kotlinUuid().next())

            When("디데이 추가를 요청한다") {
                val id = context.useCase(parameter = AddDDayUseCase.Parameter(input = input, tagIdSet = tagIdSet)).shouldBeSuccess()

                Then("TC-b8ac4b12-c88f-4031-b4dc-a62967cdf601 미삭제 상태로 생성·수정 시각이 모두 현재 시각이다") {
                    val saved = context.savedList.single()
                    saved.id shouldBe id
                    saved.isDeleted shouldBe false
                    saved.createdAt shouldBe context.now
                    saved.updatedAt shouldBe context.now
                }

                Then("TC-aafc6f21-fcfa-472b-af7e-fe0cb067b6f4 TC-5c49ab50-b5f1-497b-8540-8d7c1b32f4b9 현재 계정과 고른 태그와 함께 한 번에 저장한다") {
                    coVerify(exactly = 1) { context.repository.upsert(account = checkNotNull(context.account), dday = any(), tagIdSet = tagIdSet) }
                }

                Then("TC-59804ef8-703f-41a2-b0fd-7747b4210ab8 기기에 저장한 뒤 동기화를 한 번 요청한다") {
                    coVerifyOrder {
                        context.repository.upsert(account = any(), dday = any(), tagIdSet = any())
                        context.requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                    }
                    coVerify(exactly = 1) { context.requestSyncUseCase(parameter = any()) }
                }

                Then("TC-21d29a4d-2bf4-4c58-ac6c-d6844fc8cfc3 입력한 내용과 기록한 반복 간격을 디데이 정보로 저장한다") {
                    val saved = context.savedList.single().detail
                    saved.title shouldBe input.title
                    saved.description shouldBe input.description
                    saved.color shouldBe input.color
                    saved.baseDate shouldBe input.baseDate
                    saved.countsBaseDay shouldBe input.countsBaseDay
                    saved.repeats shouldBe input.repeatList.toRepeatSetOrNull()
                    saved.endDate shouldBe input.endDate
                    saved.overflowPolicy shouldBe input.overflowPolicy
                }
            }
        }

        Given("현재 계정 조회가 실패한다") {
            val throwable = IllegalStateException(Arb.string().next())
            val context = AddContext(accountResult = Result.failure(throwable))

            When("디데이 추가를 요청한다") {
                val result = context.useCase(parameter = parameter(input = Arb.ddayAddInput().next()))

                Then("TC-463775d9-1a15-47a3-bafe-2e1246022b15 저장하지 않고 실패를 전달한다") {
                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                    context.verifyNothingSaved()
                }
            }
        }

        Given("로그인하지 않아 게스트 계정이 확인된다") {
            val context = AddContext(accountResult = Result.success(Account.Guest))

            When("디데이 추가를 요청한다") {
                context.useCase(parameter = parameter(input = Arb.ddayAddInput().next())).shouldBeSuccess()

                Then("TC-6e0d159f-39f4-4ea0-bf57-9b8dbd3d7d37 게스트 계정 기준으로 기기에 저장한다") {
                    coVerify(exactly = 1) { context.repository.upsert(account = Account.Guest, dday = any(), tagIdSet = any()) }
                }
            }
        }

        Given("기기 저장이 실패한다") {
            val throwable = IllegalStateException(Arb.string().next())
            val context = AddContext()
            coEvery { context.repository.upsert(account = any(), dday = any(), tagIdSet = any()) } throws throwable

            When("디데이 추가를 요청한다") {
                val result = context.useCase(parameter = parameter(input = Arb.ddayAddInput().next(), tagIdSet = setOf(Arb.kotlinUuid().next())))

                Then("TC-0c20d174-5a50-4dce-8844-fdcd2b64e1e4 추가를 성공으로 다루지 않고 동기화를 요청하지 않는다") {
                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                    coVerify(exactly = 0) { context.requestSyncUseCase(parameter = any()) }
                }
            }
        }

        Given("기기 저장은 성공하고 서버 반영을 요청하는 동기화가 실패한다") {
            val context = AddContext()
            coEvery { context.requestSyncUseCase(parameter = any()) } returns Result.failure(IllegalStateException(Arb.string().next()))

            When("디데이 추가를 요청한다") {
                val result = context.useCase(parameter = parameter(input = Arb.ddayAddInput().next()))

                Then("TC-77b71541-e31d-4ec9-af09-8b60ec5771d0 기기에 저장한 결과로 성공을 전달한다") {
                    result.shouldBeSuccess() shouldBe context.savedList.single().id
                }
            }
        }
    })

private fun parameter(
    input: io.github.taetae98coding.diary.domain.dday.DDayInput,
    tagIdSet: Set<Uuid> = emptySet(),
): AddDDayUseCase.Parameter = AddDDayUseCase.Parameter(input = input, tagIdSet = tagIdSet)

private class AddContext(
    accountResult: Result<Account> = Result.success(Arb.accountUser().next()),
) {
    val account: Account? = accountResult.getOrNull()
    val now: Instant = Arb.secondPrecisionInstant().next()
    val savedList: MutableList<DDay> = mutableListOf()

    val repository: AccountDDayRepository =
        mockk<AccountDDayRepository>().also { repository ->
            coEvery { repository.upsert(account = any(), dday = any(), tagIdSet = any()) } answers { savedList += secondArg<DDay>() }
        }

    val requestSyncUseCase: RequestSyncUseCase =
        mockk<RequestSyncUseCase>().also { useCase ->
            coEvery { useCase(parameter = any()) } returns Result.success(Unit)
        }

    val useCase: AddDDayUseCase =
        AddDDayUseCase(
            getAccountUseCase =
                mockk<GetAccountUseCase>().also { useCase ->
                    every { useCase(parameter = Unit) } returns flowOf(accountResult)
                },
            requestSyncUseCase = requestSyncUseCase,
            accountDDayRepository = repository,
            clock =
                mockk<Clock>().also { clock ->
                    every { clock.now() } returns now
                },
        )

    fun verifyNothingSaved() {
        coVerify(exactly = 0) { repository.upsert(account = any(), dday = any(), tagIdSet = any()) }
        coVerify(exactly = 0) { requestSyncUseCase(parameter = any()) }
    }
}
