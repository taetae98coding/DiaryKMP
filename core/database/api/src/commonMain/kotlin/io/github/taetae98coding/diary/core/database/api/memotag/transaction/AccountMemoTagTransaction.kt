package io.github.taetae98coding.diary.core.database.api.memotag.transaction

import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountMemoTagTransaction {
    public suspend fun upsert(
        accountId: Uuid,
        memoId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    )

    public suspend fun updatePrimaryTagId(
        accountId: Uuid,
        memoId: Uuid,
        primaryTagId: Uuid?,
        updatedAt: Instant,
    )
}
