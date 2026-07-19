package io.github.taetae98coding.diary.core.database.api.web.transaction

import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import kotlin.uuid.Uuid

public interface AccountWebSyncTransaction {
    public suspend fun clearPending(
        accountId: Uuid,
        webList: List<WebLocalEntity>,
    )

    public suspend fun save(
        accountId: Uuid,
        webList: List<WebLocalEntity>,
        cursor: Long,
    )
}
