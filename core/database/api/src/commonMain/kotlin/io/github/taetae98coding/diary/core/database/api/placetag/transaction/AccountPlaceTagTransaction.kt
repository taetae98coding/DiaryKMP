package io.github.taetae98coding.diary.core.database.api.placetag.transaction

import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountPlaceTagTransaction {
    public suspend fun upsert(
        accountId: Uuid,
        placeId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    )
}
