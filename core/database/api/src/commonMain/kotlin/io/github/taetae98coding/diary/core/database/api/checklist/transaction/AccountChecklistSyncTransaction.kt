package io.github.taetae98coding.diary.core.database.api.checklist.transaction

import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import kotlin.uuid.Uuid

public interface AccountChecklistSyncTransaction {
    public suspend fun clearPending(
        accountId: Uuid,
        checklistList: List<ChecklistLocalEntity>,
    )

    public suspend fun upsert(
        accountId: Uuid,
        checklistList: List<ChecklistLocalEntity>,
        cursor: Long,
    )
}
