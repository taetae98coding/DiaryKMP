package io.github.taetae98coding.diary.work.daily.memo

import io.github.taetae98coding.diary.domain.memo.DailyMemoNotificationManager
import io.github.taetae98coding.diary.notification.Notifier
import io.github.taetae98coding.diary.work.daily.memo.di.DailyMemoNotificationScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import kotlin.time.Clock

@Module
@Configuration
public class JvmWasmWorkDailyMemoModule {
    @Single
    @DailyMemoNotificationScope
    internal fun providesDailyMemoNotificationCoroutineScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { _, _ -> })

    @Single
    internal fun providesDailyMemoNotificationManager(
        clock: Clock,
        notifier: Notifier,
        @DailyMemoNotificationScope scope: CoroutineScope,
    ): DailyMemoNotificationManager =
        TimerDailyMemoNotificationManager(
            clock = clock,
            notifier = notifier,
            scope = scope,
        )
}
