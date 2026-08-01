package io.github.taetae98coding.diary.domain.memo.usecase

import io.github.taetae98coding.diary.domain.memo.DailyMemoNotificationManager
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.datetime.LocalTime

class ScheduleDailyMemoNotificationUseCaseTest :
    BehaviorSpec({
        Given("앱이 일일 메모 알림을 예약할 수 있다") {
            When("알림 예약을 요청한다") {
                Then("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-001 알림은 오전 8시 정각에 발생하도록 한 번 예약된다") {
                    val dailyMemoNotificationManager = mockk<DailyMemoNotificationManager>(relaxed = true)
                    val useCase = ScheduleDailyMemoNotificationUseCase(dailyMemoNotificationManager = dailyMemoNotificationManager)

                    val result = useCase(parameter = Unit)

                    result.shouldBeSuccess(Unit)
                    coVerify(exactly = 1) { dailyMemoNotificationManager.schedule(time = LocalTime(hour = 8, minute = 0)) }
                }
            }
        }

        Given("알림 예약이 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException("schedule error")
            val dailyMemoNotificationManager = mockk<DailyMemoNotificationManager>()
            coEvery { dailyMemoNotificationManager.schedule(time = any()) } throws throwable
            val useCase = ScheduleDailyMemoNotificationUseCase(dailyMemoNotificationManager = dailyMemoNotificationManager)

            When("알림 예약을 요청한다") {
                Then("예약 실패를 전달한다") {
                    useCase(parameter = Unit).shouldBeFailure() shouldBeSameInstanceAs throwable
                }
            }
        }
    })
