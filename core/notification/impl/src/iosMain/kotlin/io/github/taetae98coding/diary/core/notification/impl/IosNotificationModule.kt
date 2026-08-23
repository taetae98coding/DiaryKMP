package io.github.taetae98coding.diary.core.notification.impl

import io.github.taetae98coding.diary.core.notification.api.DailyMemoNotificationScheduler
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
public class IosNotificationModule {
    @Factory
    internal fun providesDailyMemoNotificationScheduler(): DailyMemoNotificationScheduler = IosDailyMemoNotificationScheduler()
}
