package io.github.taetae98coding.diary.core.work.api

import kotlin.uuid.Uuid

public fun interface SyncWork {
    public suspend fun doWork(accountId: Uuid)
}
