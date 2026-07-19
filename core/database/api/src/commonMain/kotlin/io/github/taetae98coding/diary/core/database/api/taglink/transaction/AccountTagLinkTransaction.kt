package io.github.taetae98coding.diary.core.database.api.taglink.transaction

import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountTagLinkTransaction {
    public suspend fun upsert(
        accountId: Uuid,
        fromTagId: Uuid,
        toTagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    )
}
