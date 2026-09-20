package io.github.taetae98coding.diary.domain.memo

import io.github.taetae98coding.diary.core.model.memo.UpcomingDailyMemoNotification
import kotlinx.datetime.LocalTime

public interface DailyMemoNotificationManager {
    public suspend fun schedule(time: LocalTime)

    public suspend fun submitUpcoming(
        time: LocalTime,
        upcomingList: List<UpcomingDailyMemoNotification>,
    )
}
