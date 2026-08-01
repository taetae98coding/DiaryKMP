package io.github.taetae98coding.diary.data.memo.notification

import io.github.taetae98coding.diary.core.notification.api.DailyMemoNotificationScheduler
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.datetime.LocalTime

class DailyMemoNotificationManagerImplTest :
    BehaviorSpec({
        Given("알림 예약을 수행하는 플랫폼 예약기가 있다") {
            val dailyMemoNotificationScheduler = mockk<DailyMemoNotificationScheduler>(relaxed = true)
            val manager = DailyMemoNotificationManagerImpl(dailyMemoNotificationScheduler = dailyMemoNotificationScheduler)

            When("발생 시각을 정해 알림 예약을 요청한다") {
                Then("TC-DAILY-MEMO-NOTIFICATION-DOMAIN-001 같은 시각으로 예약이 한 번 요청된다") {
                    val time = LocalTime(hour = 8, minute = 0)

                    manager.schedule(time = time)

                    coVerify(exactly = 1) { dailyMemoNotificationScheduler.schedule(time = time) }
                }
            }
        }
    })
