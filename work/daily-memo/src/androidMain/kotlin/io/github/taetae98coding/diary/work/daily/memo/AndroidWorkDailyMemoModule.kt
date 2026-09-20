package io.github.taetae98coding.diary.work.daily.memo

import android.content.Context
import io.github.taetae98coding.diary.domain.memo.DailyMemoNotificationManager
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
}
