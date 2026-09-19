package io.github.taetae98coding.diary.core.work.impl

import io.github.taetae98coding.diary.core.work.api.PeriodicSyncWorkScheduler
import io.github.taetae98coding.diary.core.work.api.SyncWork
import io.github.taetae98coding.diary.core.work.impl.di.SyncWorkScope
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
public class JvmWasmWorkModule {
    @Single
    internal fun providesPeriodicSyncWorkScheduler(
        syncWork: SyncWork,
        @SyncWorkScope scope: CoroutineScope,
    ): PeriodicSyncWorkScheduler =
        CoroutinePeriodicSyncWorkScheduler(
            syncWork = syncWork,
            scope = scope,
        )
}
