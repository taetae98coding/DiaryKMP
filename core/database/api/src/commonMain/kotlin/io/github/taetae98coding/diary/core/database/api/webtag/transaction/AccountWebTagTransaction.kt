package io.github.taetae98coding.diary.core.database.api.webtag.transaction

import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountWebTagTransaction {
    public suspend fun upsert(
        accountId: Uuid,
        webId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    )
}
