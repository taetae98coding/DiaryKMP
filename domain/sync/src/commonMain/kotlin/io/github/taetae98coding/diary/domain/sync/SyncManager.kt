package io.github.taetae98coding.diary.domain.sync

import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

public interface SyncManager {
    public val isProgressReported: Flow<Boolean>

    public fun requestSync(reportsProgress: Boolean)

    public fun schedulePeriodicSync(period: Duration)

    public fun cancelPeriodicSync()
}
