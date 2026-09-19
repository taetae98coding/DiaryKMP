package io.github.taetae98coding.diary.core.work.api

import kotlin.time.Duration
import kotlin.uuid.Uuid

public interface PeriodicSyncWorkScheduler {
    public fun schedule(
        accountId: Uuid,
        period: Duration,
    )

    public fun cancel()
}
