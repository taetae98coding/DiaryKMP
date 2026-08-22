package io.github.taetae98coding.diary.domain.sync.usecase

import app.cash.turbine.test
import io.github.taetae98coding.diary.domain.sync.SyncManager
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeSuccess
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

class GetProgressReportedUseCaseTest :
    BehaviorSpec({
        Given("진행 표시 여부를 관찰하고 있다") {
            val isProgressReportedFlow = MutableStateFlow(false)
            val syncManager = mockk<SyncManager>()
            every { syncManager.isProgressReported } returns isProgressReportedFlow
            val useCase = GetProgressReportedUseCase(syncManager = syncManager)

            When("진행 표시 여부가 바뀐다") {
                Then("바뀐 값이 그대로 전달된다") {
                    useCase(parameter = Unit).test {
                        awaitItem().shouldBeSuccess(false)

                        isProgressReportedFlow.value = true
                        awaitItem().shouldBeSuccess(true)

                        isProgressReportedFlow.value = false
                        awaitItem().shouldBeSuccess(false)
                    }
                }
            }
        }
    })
