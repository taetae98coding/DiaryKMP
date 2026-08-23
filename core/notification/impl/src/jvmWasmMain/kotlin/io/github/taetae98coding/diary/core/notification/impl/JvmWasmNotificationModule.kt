package io.github.taetae98coding.diary.core.notification.impl

import io.github.taetae98coding.diary.core.notification.api.DailyMemoNotificationScheduler
import io.github.taetae98coding.diary.core.notification.impl.di.DailyMemoNotificationScope
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
public class JvmWasmNotificationModule {
    @Single
    @DailyMemoNotificationScope
    internal fun providesDailyMemoNotificationCoroutineScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { _, _ -> })

    @Single
    internal fun providesDailyMemoNotificationScheduler(
        clock: Clock,
        notifier: DailyMemoNotifier,
        @DailyMemoNotificationScope scope: CoroutineScope,
    ): DailyMemoNotificationScheduler =
        TimerDailyMemoNotificationScheduler(
            clock = clock,
            notifier = notifier,
            scope = scope,
        )
}
