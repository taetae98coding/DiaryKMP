package io.github.taetae98coding.diary.domain.memo.usecase

import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.memo.DailyMemoNotificationManager
import org.koin.core.annotation.Factory

@Factory
public class ScheduleDailyMemoNotificationUseCase internal constructor(
    private val dailyMemoNotificationManager: DailyMemoNotificationManager,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        dailyMemoNotificationManager.schedule(time = DAILY_MEMO_NOTIFICATION_TIME)
    }
}
