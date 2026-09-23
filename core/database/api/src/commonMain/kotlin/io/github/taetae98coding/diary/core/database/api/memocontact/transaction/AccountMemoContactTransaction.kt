package io.github.taetae98coding.diary.core.database.api.memocontact.transaction

import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountMemoContactTransaction {
    public suspend fun upsert(
        accountId: Uuid,
        memoId: Uuid,
        contactId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    )
}
