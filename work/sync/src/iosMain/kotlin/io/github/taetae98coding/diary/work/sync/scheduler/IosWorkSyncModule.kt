package io.github.taetae98coding.diary.work.sync.scheduler

import io.github.taetae98coding.diary.work.sync.di.SyncScope
import io.github.taetae98coding.diary.work.sync.scheduler.PeriodicSyncWorkScheduler
import io.github.taetae98coding.diary.work.sync.work.SyncWork
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
public class IosWorkSyncModule {
    @Single
    internal fun providesBackgroundTaskPeriodicSyncWorkScheduler(
        syncWork: SyncWork,
        @SyncScope scope: CoroutineScope,
    ): BackgroundTaskPeriodicSyncWorkScheduler =
        BackgroundTaskPeriodicSyncWorkScheduler(
            syncWork = syncWork,
            scope = scope,
        )

    @Single
    internal fun providesPeriodicSyncWorkScheduler(scheduler: BackgroundTaskPeriodicSyncWorkScheduler): PeriodicSyncWorkScheduler = scheduler
}
