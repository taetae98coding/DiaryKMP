package io.github.taetae98coding.diary.work.sync.scheduler

import kotlinx.coroutines.flow.Flow

internal interface SyncWorkScheduler {
    val state: Flow<SyncWorkState>

    fun sync()
}
