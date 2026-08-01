package io.github.taetae98coding.diary.data.memo.notification

import io.github.taetae98coding.diary.core.notification.api.DailyMemoNotificationScheduler
import io.github.taetae98coding.diary.domain.memo.DailyMemoNotificationManager
import kotlinx.datetime.LocalTime
import org.koin.core.annotation.Factory

@Factory
internal class DailyMemoNotificationManagerImpl(
    private val dailyMemoNotificationScheduler: DailyMemoNotificationScheduler,
) : DailyMemoNotificationManager {
    override suspend fun schedule(time: LocalTime) {
        dailyMemoNotificationScheduler.schedule(time = time)
    }
}
