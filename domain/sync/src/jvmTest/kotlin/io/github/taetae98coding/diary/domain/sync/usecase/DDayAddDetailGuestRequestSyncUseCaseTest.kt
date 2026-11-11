package io.github.taetae98coding.diary.domain.sync.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.sync.SyncManager
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeSuccess
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf

class DDayAddDetailGuestRequestSyncUseCaseTest :
    BehaviorSpec({
        Given("로그인하지 않아 게스트 계정이 확인된다") {
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(Account.Guest))
            val syncManager = mockk<SyncManager>(relaxed = true)
            val useCase = RequestSyncUseCase(getAccountUseCase = getAccountUseCase, syncManager = syncManager)

            When("디데이 추가, 수정, 삭제나 삭제의 실행 취소를 저장한 뒤 동기화를 요청한다") {
                Then("TC-6e0d159f-39f4-4ea0-bf57-9b8dbd3d7d37 TC-c880c2ae-beb7-4d16-8d35-aceafa03dae2 TC-31cf25b9-56c6-49f0-acd4-7f6ca2feb139 동기화를 요청하지 않는다") {
                    useCase(parameter = SyncTrigger.DATA_CHANGED).shouldBeSuccess(Unit)

                    verify(exactly = 0) { syncManager.requestSync(reportsProgress = any()) }
                }
            }
        }
    })
