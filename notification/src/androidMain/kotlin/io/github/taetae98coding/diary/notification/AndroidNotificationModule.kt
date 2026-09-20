package io.github.taetae98coding.diary.notification

import android.content.Context
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
public class AndroidNotificationModule {
    @Factory
    internal fun providesNotifier(context: Context): Notifier = AndroidNotifier(context = context)
}
