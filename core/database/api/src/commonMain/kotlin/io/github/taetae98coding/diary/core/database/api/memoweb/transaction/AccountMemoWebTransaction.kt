package io.github.taetae98coding.diary.core.database.api.memoweb.transaction

import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountMemoWebTransaction {
    public suspend fun upsert(
        accountId: Uuid,
        memoId: Uuid,
        webId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    )
}
