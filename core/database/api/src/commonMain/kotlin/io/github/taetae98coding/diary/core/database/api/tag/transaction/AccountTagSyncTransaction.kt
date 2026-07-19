package io.github.taetae98coding.diary.core.database.api.tag.transaction

import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import kotlin.uuid.Uuid

public interface AccountTagSyncTransaction {
    public suspend fun clearPending(
        accountId: Uuid,
        tagList: List<TagLocalEntity>,
    )

    public suspend fun save(
        accountId: Uuid,
        tagList: List<TagLocalEntity>,
        cursor: Long,
    )
}
