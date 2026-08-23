package io.github.taetae98coding.diary.core.notification.impl

import android.content.Context
import io.github.taetae98coding.diary.core.notification.api.DailyMemoNotificationScheduler
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import kotlin.time.Clock

@Module
@Configuration
public class AndroidNotificationModule {
    @Factory
    internal fun providesDailyMemoNotificationScheduler(
        context: Context,
        clock: Clock,
    ): DailyMemoNotificationScheduler =
        AndroidDailyMemoNotificationScheduler(
            context = context,
            clock = clock,
        )
}
