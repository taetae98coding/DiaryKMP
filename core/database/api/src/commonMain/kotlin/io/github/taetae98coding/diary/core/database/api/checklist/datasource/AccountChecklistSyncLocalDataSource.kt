package io.github.taetae98coding.diary.core.database.api.checklist.datasource

import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import kotlin.uuid.Uuid

public interface AccountChecklistSyncLocalDataSource {
    public suspend fun readPendingList(accountId: Uuid): List<ChecklistLocalEntity>
}
