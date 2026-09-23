@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.more.ui.home.refresh

import io.github.taetae98coding.diary.domain.account.usecase.RefreshUserDataUseCase
import io.kotest.core.spec.style.FunSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class MoreHomeRefreshViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-MORE-HOME-FEATURE-033 앞선 확인이 끝난 뒤 다시 요청하면 사용자 정보 다시 확인을 다시 시작한다") {
            runTest(mainDispatcher) {
                val useCase = mockk<RefreshUserDataUseCase>()
                coEvery { useCase(Unit) } returns Result.success(Unit)
                val viewModel = MoreHomeRefreshViewModel(refreshUserDataUseCase = useCase)

                viewModel.refresh()
                advanceUntilIdle()
                viewModel.refresh()
                advanceUntilIdle()

                coVerify(exactly = 2) { useCase(Unit) }
            }
        }

        test("앞선 확인이 진행 중이면 새 확인을 겹쳐 시작하지 않는다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Unit>()
                val useCase = mockk<RefreshUserDataUseCase>()
                coEvery { useCase(Unit) } coAnswers { Result.success(completion.await()) }
                val viewModel = MoreHomeRefreshViewModel(refreshUserDataUseCase = useCase)

                viewModel.refresh()
                runCurrent()
                viewModel.refresh()
                runCurrent()
                completion.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(Unit) }
            }
        }

        test("TC-MORE-HOME-DOMAIN-013 사용자 정보 다시 확인에 실패해도 아무것도 알리지 않는다") {
            runTest(mainDispatcher) {
                val useCase = mockk<RefreshUserDataUseCase>()
                coEvery { useCase(Unit) } returns Result.failure(IllegalStateException("refresh failed"))
                val viewModel = MoreHomeRefreshViewModel(refreshUserDataUseCase = useCase)

                viewModel.refresh()
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(Unit) }
            }
        }
    }
}
