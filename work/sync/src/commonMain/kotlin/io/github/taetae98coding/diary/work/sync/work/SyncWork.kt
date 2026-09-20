package io.github.taetae98coding.diary.work.sync.work

import kotlin.uuid.Uuid

internal fun interface SyncWork {
    suspend fun doWork(accountId: Uuid)
}
