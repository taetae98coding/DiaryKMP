package io.github.taetae98coding.diary.core.database.api.entitytag.transaction

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import kotlin.uuid.Uuid

public interface AccountEntityTagSyncTransaction {
    public suspend fun clearPending(
        accountId: Uuid,
        kind: EntityTagKindLocalEntity,
        entityTagList: List<EntityTagLocalEntity>,
    )

    public suspend fun upsert(
        accountId: Uuid,
        kind: EntityTagKindLocalEntity,
        entityTagList: List<EntityTagLocalEntity>,
        cursor: Long,
    )
}
