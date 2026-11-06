package io.github.taetae98coding.diary.core.database.api.entitytag.datasource

import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagKindLocalEntity
import io.github.taetae98coding.diary.core.database.api.entitytag.entity.EntityTagLocalEntity
import kotlin.uuid.Uuid

public interface AccountEntityTagSyncLocalDataSource {
    public suspend fun readPendingList(
        accountId: Uuid,
        kind: EntityTagKindLocalEntity,
    ): List<EntityTagLocalEntity>
}
