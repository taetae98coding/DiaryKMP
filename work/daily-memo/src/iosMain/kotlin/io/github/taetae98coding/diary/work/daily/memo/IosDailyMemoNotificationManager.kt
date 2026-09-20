package io.github.taetae98coding.diary.work.daily.memo

import io.github.taetae98coding.diary.core.model.memo.UpcomingDailyMemoNotification
import io.github.taetae98coding.diary.domain.memo.DailyMemoNotificationManager
import io.github.taetae98coding.diary.notification.LocalNotificationRequest
import io.github.taetae98coding.diary.notification.LocalNotificationScheduler
import kotlinx.datetime.LocalTime
import kotlinx.datetime.atTime
import org.koin.core.annotation.Factory

@Factory
internal class IosDailyMemoNotificationManager(
    private val localNotificationScheduler: LocalNotificationScheduler,
) : DailyMemoNotificationManager {
    // iOS는 발생 시점에 앱 코드가 실행되지 않아 내용을 미리 정한 날짜별 알림으로 대신하므로 매일 반복 예약을 두지 않는다. 함께 두면 같은 시각에 알림이 두 번 뜬다.
    override suspend fun schedule(time: LocalTime) = Unit

    override suspend fun submitUpcoming(
        time: LocalTime,
        upcomingList: List<UpcomingDailyMemoNotification>,
    ) {
        localNotificationScheduler.submit(
            identifierPrefix = DAILY_MEMO_NOTIFICATION_ID,
            requestList =
                upcomingList.map { upcoming ->
                    LocalNotificationRequest(
                        notification = dailyMemoNotification(upcoming = upcoming),
                        dateTime = upcoming.date.atTime(time),
                    )
                },
        )
    }
}
