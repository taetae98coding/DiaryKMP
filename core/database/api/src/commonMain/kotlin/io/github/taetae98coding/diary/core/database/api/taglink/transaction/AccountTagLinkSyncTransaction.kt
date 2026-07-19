package io.github.taetae98coding.diary.core.database.api.taglink.transaction

import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import kotlin.uuid.Uuid

public interface AccountTagLinkSyncTransaction {
    public suspend fun clearPending(
        accountId: Uuid,
        tagLinkList: List<TagLinkLocalEntity>,
    )

    public suspend fun save(
        accountId: Uuid,
        tagLinkList: List<TagLinkLocalEntity>,
        cursor: Long,
    )
}
