package io.github.taetae98coding.diary.work.sync.scheduler

import io.github.taetae98coding.diary.work.sync.di.PeriodicSyncDispatcher
import kotlinx.coroutines.CoroutineDispatcher
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
}
