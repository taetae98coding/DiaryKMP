package io.github.taetae98coding.diary.core.datastore.api.sync.datasource

import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountSyncTimeLocalDataSource {
    public suspend fun find(accountId: Uuid): Instant?

    public suspend fun upsert(
        accountId: Uuid,
        syncedAt: Instant,
    )
}
