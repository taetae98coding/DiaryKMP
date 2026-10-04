package io.github.taetae98coding.diary.work.sync

import io.github.taetae98coding.diary.library.coroutines.scope.workCoroutineScope
import io.github.taetae98coding.diary.work.sync.di.SyncScope
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
@ComponentScan
public class WorkSyncModule {
    @Single
    @SyncScope
    internal fun providesSyncCoroutineScope(): CoroutineScope = workCoroutineScope()
}
