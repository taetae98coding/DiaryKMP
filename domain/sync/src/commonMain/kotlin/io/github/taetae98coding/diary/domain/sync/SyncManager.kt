package io.github.taetae98coding.diary.domain.sync

import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration
import kotlin.uuid.Uuid

public interface SyncManager {
    public val isProgressReported: Flow<Boolean>

    public fun requestSync(
        accountId: Uuid,
        reportsProgress: Boolean,
    )

    public fun schedulePeriodicSync(
        accountId: Uuid,
        period: Duration,
    )

    public fun cancelPeriodicSync()
}
