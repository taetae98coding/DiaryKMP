package io.github.taetae98coding.diary.work.sync.scheduler

import kotlin.time.Duration

internal interface PeriodicSyncWorkScheduler {
    fun schedule(period: Duration)

    fun cancel()
}
