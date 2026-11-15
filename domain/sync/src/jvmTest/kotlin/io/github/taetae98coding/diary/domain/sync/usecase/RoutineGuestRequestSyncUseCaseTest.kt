package io.github.taetae98coding.diary.domain.sync.usecase

import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.sync.SyncManager
import io.github.taetae98coding.diary.domain.sync.SyncTrigger
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeSuccess
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf

class RoutineGuestRequestSyncUseCaseTest :
    BehaviorSpec({
        Given("로그인하지 않아 게스트 계정이 확인된다") {
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase() } returns flowOf(Result.success(Account.Guest))
            val syncManager = mockk<SyncManager>(relaxed = true)
            val useCase = RequestSyncUseCase(getAccountUseCase = getAccountUseCase, syncManager = syncManager)

            When("루틴 추가, 수정, 완료나 다시 시작, 삭제나 삭제의 실행 취소, 기록 넣기나 빼기를 저장한 뒤 동기화를 요청한다") {
                Then("TC-7ad79a89-3a76-4336-9674-901ee9ce9d57 TC-24a46e88-852d-48d7-8837-2e9c2dea8338 TC-5d2c34e1-fd3e-4b4e-9075-0357e5ceee91 TC-b2068d06-812b-4553-a5c3-a6cb17f5b483 TC-c73d0aa4-86f5-4ef5-8f10-8fd64f7f8c57 동기화를 요청하지 않는다") {
                    useCase(parameter = SyncTrigger.DATA_CHANGED).shouldBeSuccess(Unit)

                    verify(exactly = 0) { syncManager.requestSync(reportsProgress = any()) }
                }
            }
        }
    })
