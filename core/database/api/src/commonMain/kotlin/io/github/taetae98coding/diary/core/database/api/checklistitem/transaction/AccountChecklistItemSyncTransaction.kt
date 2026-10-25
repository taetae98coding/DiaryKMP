package io.github.taetae98coding.diary.core.database.api.checklistitem.transaction

import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemLocalEntity
import kotlin.uuid.Uuid

public interface AccountChecklistItemSyncTransaction {
    public suspend fun clearPending(
        accountId: Uuid,
        itemList: List<ChecklistItemLocalEntity>,
    )

    public suspend fun upsert(
        accountId: Uuid,
        itemList: List<ChecklistItemLocalEntity>,
        cursor: Long,
    )
}
