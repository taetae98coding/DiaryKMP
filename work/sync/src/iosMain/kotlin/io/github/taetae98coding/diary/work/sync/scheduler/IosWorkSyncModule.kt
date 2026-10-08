package io.github.taetae98coding.diary.work.sync.scheduler

import io.github.taetae98coding.diary.work.sync.di.PeriodicSyncDispatcher
import io.github.taetae98coding.diary.work.sync.di.SyncScope
import io.github.taetae98coding.diary.work.sync.scheduler.PeriodicSyncWorkScheduler
import io.github.taetae98coding.diary.work.sync.work.SyncWork
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
public class IosWorkSyncModule {
    // 요청한 순서대로 실행되도록 한 번에 하나만 실행하는 dispatcher를 하나만 둔다.
    @Single
    @PeriodicSyncDispatcher
    internal fun providesPeriodicSyncDispatcher(): CoroutineDispatcher = Dispatchers.Default.limitedParallelism(1)

    @Single
    internal fun providesBackgroundTaskPeriodicSyncWorkScheduler(
        syncWork: SyncWork,
        @SyncScope scope: CoroutineScope,
        @PeriodicSyncDispatcher dispatcher: CoroutineDispatcher,
    ): BackgroundTaskPeriodicSyncWorkScheduler =
        BackgroundTaskPeriodicSyncWorkScheduler(
            syncWork = syncWork,
            scope = scope,
            dispatcher = dispatcher,
        )

    @Single
    internal fun providesPeriodicSyncWorkScheduler(scheduler: BackgroundTaskPeriodicSyncWorkScheduler): PeriodicSyncWorkScheduler = scheduler
}
