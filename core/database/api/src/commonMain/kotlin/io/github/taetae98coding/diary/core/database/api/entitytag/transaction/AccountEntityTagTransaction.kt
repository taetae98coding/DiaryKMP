package io.github.taetae98coding.diary.core.database.api.entitytag.transaction

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

public interface AccountEntityTagTransaction {
    public suspend fun upsert(
        accountId: Uuid,
        kind: EntityTagKindLocalEntity,
        entityId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    )
}
