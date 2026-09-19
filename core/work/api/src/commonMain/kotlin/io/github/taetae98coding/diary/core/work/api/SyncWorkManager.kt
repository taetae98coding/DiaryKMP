package io.github.taetae98coding.diary.core.work.api

import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration
import kotlin.uuid.Uuid

public interface SyncWorkManager {
    public val state: Flow<SyncWorkState>

    public fun sync(accountId: Uuid)

    public fun schedulePeriodicSync(
        accountId: Uuid,
        period: Duration,
    )

    public fun cancelPeriodicSync()
}
