package io.github.taetae98coding.diary.notification

import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
public class IosNotificationModule {
    @Single
    internal fun providesNotifier(): Notifier = IosNotifier()

    @Single
    internal fun providesLocalNotificationScheduler(): LocalNotificationScheduler = IosLocalNotificationScheduler()
}
