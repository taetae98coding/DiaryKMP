package io.github.taetae98coding.diary.core.database.api.memoplace.transaction

import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountMemoPlaceTransaction {
    public suspend fun upsert(
        accountId: Uuid,
        memoId: Uuid,
        placeId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    )
}
