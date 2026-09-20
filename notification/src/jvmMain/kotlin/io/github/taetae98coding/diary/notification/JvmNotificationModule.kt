package io.github.taetae98coding.diary.notification

import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
public class JvmNotificationModule {
    @Single
    internal fun providesNotifier(): Notifier = JvmNotifier()
}
