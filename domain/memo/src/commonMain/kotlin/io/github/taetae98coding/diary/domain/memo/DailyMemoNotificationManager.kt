package io.github.taetae98coding.diary.domain.memo

import kotlinx.datetime.LocalTime

public interface DailyMemoNotificationManager {
    public suspend fun schedule(time: LocalTime)
}
