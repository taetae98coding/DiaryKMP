package io.github.taetae98coding.diary.domain.dday.usecase

import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.dday.repository.AccountDDayRepository
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.github.taetae98coding.diary.domain.sync.usecase.RequestSyncUseCase
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock

class DDayPrimaryTagUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 현재 시각이 준비되어 있다") {
            val account = Arb.accountUser().next()
            val now = Arb.secondPrecisionInstant().next()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.success(account))
            val requestSyncUseCase = mockk<RequestSyncUseCase>()
            coEvery { requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED) } returns Result.success(Unit)
            val repository = mockk<AccountDDayRepository>()
            coEvery { repository.updatePrimaryTagId(account = any(), ddayId = any(), primaryTagId = any(), updatedAt = any()) } returns Arb.int(min = 0).next()
            val clock = mockk<Clock>()
            every { clock.now() } returns now

            When("디데이의 대표 태그를 지정한다") {
                val ddayId = Arb.kotlinUuid().next()
                val tagId = Arb.kotlinUuid().next()
                val useCase = SetDDayPrimaryTagUseCase(getAccountUseCase = getAccountUseCase, requestSyncUseCase = requestSyncUseCase, accountDDayRepository = repository, clock = clock)

                val result = useCase(parameter = SetDDayPrimaryTagUseCase.Parameter(ddayId = ddayId, tagId = tagId))

                Then("TC-db01c56c-7d0c-4a24-b133-c60cc14f75ef 현재 시각으로 대표 태그를 저장한 뒤 동기화를 한 번 요청한다") {
                    result.shouldBeSuccess()
                    coVerifyOrder {
                        repository.updatePrimaryTagId(account = account, ddayId = ddayId, primaryTagId = tagId, updatedAt = now)
                        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                    }
                    coVerify(exactly = 1) { requestSyncUseCase(parameter = any()) }
                }
            }

            // 추가 테스트: 해제도 같은 저장과 동기화 요청을 거친다.
            When("디데이의 대표 태그를 해제한다") {
                val ddayId = Arb.kotlinUuid().next()
                val useCase = UnsetDDayPrimaryTagUseCase(getAccountUseCase = getAccountUseCase, requestSyncUseCase = requestSyncUseCase, accountDDayRepository = repository, clock = clock)

                val result = useCase(parameter = ddayId)

                Then("현재 시각으로 대표 태그를 비운 뒤 동기화를 요청한다") {
                    result.shouldBeSuccess()
                    coVerifyOrder {
                        repository.updatePrimaryTagId(account = account, ddayId = ddayId, primaryTagId = null, updatedAt = now)
                        requestSyncUseCase(parameter = SyncTrigger.DATA_CHANGED)
                    }
                }
            }
        }
    })
