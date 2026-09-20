package io.github.taetae98coding.diary.work.daily.memo

import android.content.Context
import io.github.taetae98coding.diary.domain.memo.DailyMemoNotificationManager
import io.github.taetae98coding.diary.domain.memo.usecase.GetDailyMemoUseCase
import io.github.taetae98coding.diary.notification.Notifier
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import kotlin.time.Clock

@Module
@Configuration
public class AndroidWorkDailyMemoModule {
    @Factory
    internal fun providesDailyMemoNotificationManager(
        context: Context,
        clock: Clock,
    ): DailyMemoNotificationManager =
        AndroidDailyMemoNotificationManager(
            context = context,
            clock = clock,
        )

    @Factory
    internal fun providesDailyMemoNotificationWork(
        context: Context,
        getDailyMemoUseCase: GetDailyMemoUseCase,
        notifier: Notifier,
        clock: Clock,
    ): DailyMemoNotificationWork =
        DailyMemoNotificationWork(
            getDailyMemoUseCase = getDailyMemoUseCase,
            notifier = notifier,
            clock = clock,
            createNotification = context::dailyMemoNotification,
        )
}
