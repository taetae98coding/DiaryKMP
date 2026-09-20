package io.github.taetae98coding.diary.work.sync.scheduler

import kotlin.time.Duration
import kotlin.uuid.Uuid

internal interface PeriodicSyncWorkScheduler {
    fun schedule(
        accountId: Uuid,
        period: Duration,
    )

    fun cancel()
}
