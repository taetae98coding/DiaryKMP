@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.app

import io.github.taetae98coding.diary.domain.memo.usecase.ScheduleDailyMemoNotificationUseCase
import io.kotest.core.spec.style.FunSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class AppDailyMemoNotificationViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-DAILY-MEMO-NOTIFICATION-FEATURE-001 앱이 알림 예약을 요청하면 예약이 한 번 요청된다") {
            runTest(mainDispatcher) {
                val scheduleDailyMemoNotificationUseCase = useCase()
                val viewModel = AppDailyMemoNotificationViewModel(scheduleDailyMemoNotificationUseCase = scheduleDailyMemoNotificationUseCase)

                viewModel.schedule()
                advanceUntilIdle()

                coVerify(exactly = 1) { scheduleDailyMemoNotificationUseCase(parameter = Unit) }
            }
        }

        // 중복 예약 방어는 예약을 소유한 예약기가 하므로, 이 ViewModel은 요청을 삼키지 않는다.
        test("예약 요청이 여러 번 들어오면 그때마다 예약을 요청한다") {
            runTest(mainDispatcher) {
                val scheduleDailyMemoNotificationUseCase = useCase()
                val viewModel = AppDailyMemoNotificationViewModel(scheduleDailyMemoNotificationUseCase = scheduleDailyMemoNotificationUseCase)

                viewModel.schedule()
                advanceUntilIdle()
                viewModel.schedule()
                advanceUntilIdle()

                coVerify(exactly = 2) { scheduleDailyMemoNotificationUseCase(parameter = Unit) }
            }
        }
    }

    public companion object {
        private fun useCase(): ScheduleDailyMemoNotificationUseCase =
            mockk<ScheduleDailyMemoNotificationUseCase>().apply {
                coEvery { this@apply(parameter = Unit) } returns Result.success(Unit)
            }
    }
}
