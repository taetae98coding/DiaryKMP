@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.app

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMe
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.memo.DailyMemo
import io.github.taetae98coding.diary.core.model.memo.DailyMemoNotificationContent
import io.github.taetae98coding.diary.core.model.memo.UpcomingDailyMemoNotification
import io.github.taetae98coding.diary.domain.memo.usecase.GetUpcomingDailyMemoNotificationUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.ScheduleDailyMemoNotificationUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SubmitUpcomingDailyMemoNotificationUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

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
                val scheduleDailyMemoNotificationUseCase = scheduleUseCase()
                val viewModel = viewModel(scheduleDailyMemoNotificationUseCase = scheduleDailyMemoNotificationUseCase)

                viewModel.schedule()
                advanceUntilIdle()

                coVerify(exactly = 1) { scheduleDailyMemoNotificationUseCase(parameter = Unit) }
            }
        }

        // 중복 예약 방어는 예약을 소유한 예약기가 하므로, 이 ViewModel은 요청을 삼키지 않는다.
        test("예약 요청이 여러 번 들어오면 그때마다 예약을 요청한다") {
            runTest(mainDispatcher) {
                val scheduleDailyMemoNotificationUseCase = scheduleUseCase()
                val viewModel = viewModel(scheduleDailyMemoNotificationUseCase = scheduleDailyMemoNotificationUseCase)

                viewModel.schedule()
                advanceUntilIdle()
                viewModel.schedule()
                advanceUntilIdle()

                coVerify(exactly = 2) { scheduleDailyMemoNotificationUseCase(parameter = Unit) }
            }
        }

        test("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-017 앞으로 7일의 알림 내용이 정해지면 그 내용을 제공한다") {
            runTest(mainDispatcher) {
                val upcomingList = upcomingList()
                val viewModel = viewModel(getUpcomingDailyMemoNotificationUseCase = getUpcomingUseCase(upcomingFlow = MutableStateFlow(Result.success(upcomingList))))

                viewModel.upcoming.test {
                    awaitItem() shouldBe upcomingList
                    expectNoEvents()
                }
            }
        }

        test("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-019 알림 내용이 바뀌면 바뀐 내용을 다시 제공한다") {
            runTest(mainDispatcher) {
                val upcomingList = upcomingList()
                val changedUpcomingList = upcomingList()
                val upcomingFlow = MutableStateFlow(Result.success(upcomingList))
                val viewModel = viewModel(getUpcomingDailyMemoNotificationUseCase = getUpcomingUseCase(upcomingFlow = upcomingFlow))

                viewModel.upcoming.test {
                    awaitItem() shouldBe upcomingList

                    upcomingFlow.value = Result.success(changedUpcomingList)

                    awaitItem() shouldBe changedUpcomingList
                }
            }
        }

        test("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-017 알림 내용 제출을 요청하면 그 내용이 한 번 제출된다") {
            runTest(mainDispatcher) {
                val upcomingList = upcomingList()
                val submitUpcomingDailyMemoNotificationUseCase = submitUseCase()
                val viewModel = viewModel(submitUpcomingDailyMemoNotificationUseCase = submitUpcomingDailyMemoNotificationUseCase)

                viewModel.submitUpcoming(upcomingList = upcomingList)
                advanceUntilIdle()

                coVerify(exactly = 1) { submitUpcomingDailyMemoNotificationUseCase(parameter = upcomingList) }
            }
        }

        test("같은 알림 내용이 이어서 정해지면 한 번만 제공한다") {
            runTest(mainDispatcher) {
                val upcomingList = upcomingList()
                val upcomingFlow = MutableStateFlow(Result.success(upcomingList))
                val viewModel = viewModel(getUpcomingDailyMemoNotificationUseCase = getUpcomingUseCase(upcomingFlow = upcomingFlow))

                viewModel.upcoming.test {
                    awaitItem() shouldBe upcomingList

                    upcomingFlow.value = Result.success(upcomingList.toList())

                    expectNoEvents()
                }
            }
        }

        test("알림 내용을 정하지 못한 결과는 제공하지 않는다") {
            runTest(mainDispatcher) {
                val viewModel =
                    viewModel(
                        getUpcomingDailyMemoNotificationUseCase =
                            getUpcomingUseCase(upcomingFlow = MutableStateFlow(Result.failure(IllegalStateException("upcoming error")))),
                    )

                viewModel.upcoming.test {
                    expectNoEvents()
                }
            }
        }
    }

    public companion object {
        private fun viewModel(
            scheduleDailyMemoNotificationUseCase: ScheduleDailyMemoNotificationUseCase = scheduleUseCase(),
            getUpcomingDailyMemoNotificationUseCase: GetUpcomingDailyMemoNotificationUseCase = getUpcomingUseCase(),
            submitUpcomingDailyMemoNotificationUseCase: SubmitUpcomingDailyMemoNotificationUseCase = submitUseCase(),
        ): AppDailyMemoNotificationViewModel =
            AppDailyMemoNotificationViewModel(
                scheduleDailyMemoNotificationUseCase = scheduleDailyMemoNotificationUseCase,
                getUpcomingDailyMemoNotificationUseCase = getUpcomingDailyMemoNotificationUseCase,
                submitUpcomingDailyMemoNotificationUseCase = submitUpcomingDailyMemoNotificationUseCase,
            )

        private fun scheduleUseCase(): ScheduleDailyMemoNotificationUseCase =
            mockk<ScheduleDailyMemoNotificationUseCase>().apply {
                coEvery { this@apply(parameter = Unit) } returns Result.success(Unit)
            }

        private fun getUpcomingUseCase(upcomingFlow: MutableStateFlow<Result<List<UpcomingDailyMemoNotification>>> = MutableStateFlow(Result.success(emptyList()))): GetUpcomingDailyMemoNotificationUseCase =
            mockk<GetUpcomingDailyMemoNotificationUseCase>().apply {
                every { this@apply(parameter = Unit) } returns upcomingFlow
            }

        private fun submitUseCase(): SubmitUpcomingDailyMemoNotificationUseCase =
            mockk<SubmitUpcomingDailyMemoNotificationUseCase>().apply {
                coEvery { this@apply(parameter = any()) } returns Result.success(Unit)
            }

        private fun upcomingList(): List<UpcomingDailyMemoNotification> =
            List(size = 7) {
                UpcomingDailyMemoNotification(
                    date = fixtureMonkey.giveMeOne<LocalDate>(),
                    content = DailyMemoNotificationContent.Loaded(memoList = fixtureMonkey.giveMe<DailyMemo>(size = 2)),
                )
            }
    }
}
