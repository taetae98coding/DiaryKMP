package io.github.taetae98coding.diary.work.sync.scheduler

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.taetae98coding.diary.work.sync.di.PeriodicSyncDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@BindingContainer
@ContributesTo(AppScope::class)
public object IosWorkSyncModule {
    // 요청한 순서대로 실행되도록 한 번에 하나만 실행하는 dispatcher를 하나만 둔다.
    @Provides
    @SingleIn(AppScope::class)
    @PeriodicSyncDispatcher
    internal fun providesPeriodicSyncDispatcher(): CoroutineDispatcher = Dispatchers.Default.limitedParallelism(1)

    @Provides
    internal fun providesPeriodicSyncWorkScheduler(scheduler: BackgroundTaskPeriodicSyncWorkScheduler): PeriodicSyncWorkScheduler = scheduler
}
