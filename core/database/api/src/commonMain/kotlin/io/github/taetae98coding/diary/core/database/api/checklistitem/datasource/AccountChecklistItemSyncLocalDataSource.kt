package io.github.taetae98coding.diary.core.database.api.checklistitem.datasource

import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemLocalEntity
import kotlin.uuid.Uuid

public interface AccountChecklistItemSyncLocalDataSource {
    public suspend fun readPendingList(accountId: Uuid): List<ChecklistItemLocalEntity>
}
