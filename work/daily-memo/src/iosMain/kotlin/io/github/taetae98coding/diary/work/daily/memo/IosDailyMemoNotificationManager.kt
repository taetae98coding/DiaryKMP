package io.github.taetae98coding.diary.work.daily.memo

import io.github.taetae98coding.diary.domain.memo.DailyMemoNotificationManager
import io.github.taetae98coding.diary.notification.LocalNotificationScheduler
import kotlinx.datetime.LocalTime
import org.koin.core.annotation.Factory

private const val DAILY_MEMO_NOTIFICATION_TITLE_KEY = "daily_memo_notification_title"

@Factory
internal class IosDailyMemoNotificationManager(
    private val localNotificationScheduler: LocalNotificationScheduler,
) : DailyMemoNotificationManager {
    override suspend fun schedule(time: LocalTime) {
        localNotificationScheduler.scheduleDaily(
            identifier = DAILY_MEMO_NOTIFICATION_ID,
            titleLocalizationKey = DAILY_MEMO_NOTIFICATION_TITLE_KEY,
            time = time,
        )
    }
}
