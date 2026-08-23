package io.github.taetae98coding.diary.core.notification.api

import kotlinx.datetime.LocalTime

public interface DailyMemoNotificationScheduler {
    public suspend fun schedule(time: LocalTime)
}
