package io.github.taetae98coding.diary.domain.sync

import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountSyncTimeRepository {
    public suspend fun find(accountId: Uuid): Instant?

    public suspend fun upsert(
        accountId: Uuid,
        syncedAt: Instant,
    )
}
