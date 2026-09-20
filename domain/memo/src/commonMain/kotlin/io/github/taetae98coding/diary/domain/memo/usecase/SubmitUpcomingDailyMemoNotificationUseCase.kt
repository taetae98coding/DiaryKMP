package io.github.taetae98coding.diary.domain.memo.usecase

import io.github.taetae98coding.diary.core.model.memo.UpcomingDailyMemoNotification
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.memo.DailyMemoNotificationManager
import org.koin.core.annotation.Factory

@Factory
public class SubmitUpcomingDailyMemoNotificationUseCase internal constructor(
    private val dailyMemoNotificationManager: DailyMemoNotificationManager,
) : UseCase<List<UpcomingDailyMemoNotification>, Unit>() {
    override suspend fun execute(parameter: List<UpcomingDailyMemoNotification>) {
        dailyMemoNotificationManager.submitUpcoming(
            time = DAILY_MEMO_NOTIFICATION_TIME,
            upcomingList = parameter,
        )
    }
}
