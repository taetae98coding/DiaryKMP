package io.github.taetae98coding.diary.core.database.api.webtag.transaction

import io.github.taetae98coding.diary.core.database.api.webtag.entity.WebTagLocalEntity
import kotlin.uuid.Uuid

public interface AccountWebTagSyncTransaction {
    public suspend fun clearPending(
        accountId: Uuid,
        webTagList: List<WebTagLocalEntity>,
    )

    public suspend fun save(
        accountId: Uuid,
        webTagList: List<WebTagLocalEntity>,
        cursor: Long,
    )
}
