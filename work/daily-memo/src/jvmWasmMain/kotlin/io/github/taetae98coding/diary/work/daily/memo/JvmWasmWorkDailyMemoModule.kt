package io.github.taetae98coding.diary.work.daily.memo

import io.github.taetae98coding.diary.domain.memo.DailyMemoNotificationManager
import io.github.taetae98coding.diary.domain.memo.usecase.GetDailyMemoUseCase
import io.github.taetae98coding.diary.notification.Notifier
import io.github.taetae98coding.diary.work.daily.memo.di.DailyMemoNotificationScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import kotlin.time.Clock

@Module
@Configuration
public class JvmWasmWorkDailyMemoModule {
    @Single
    @DailyMemoNotificationScope
    internal fun providesDailyMemoNotificationCoroutineScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { _, _ -> })

    @Factory
    internal fun providesDailyMemoNotificationWork(
        getDailyMemoUseCase: GetDailyMemoUseCase,
        notifier: Notifier,
        clock: Clock,
    ): DailyMemoNotificationWork =
        DailyMemoNotificationWork(
            getDailyMemoUseCase = getDailyMemoUseCase,
            notifier = notifier,
            clock = clock,
            createNotification = ::dailyMemoNotification,
        )

    @Single
    internal fun providesDailyMemoNotificationManager(
        clock: Clock,
        work: DailyMemoNotificationWork,
        @DailyMemoNotificationScope scope: CoroutineScope,
    ): DailyMemoNotificationManager =
        TimerDailyMemoNotificationManager(
            clock = clock,
            work = work,
            scope = scope,
        )
}
