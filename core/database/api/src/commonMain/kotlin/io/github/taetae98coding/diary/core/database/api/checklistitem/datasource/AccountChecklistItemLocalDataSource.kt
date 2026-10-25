package io.github.taetae98coding.diary.core.database.api.checklistitem.datasource

import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemSortLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountChecklistItemLocalDataSource {
    public fun get(
        accountId: Uuid,
        checklistId: Uuid,
        sort: ChecklistItemSortLocalEntity,
    ): Flow<List<ChecklistItemLocalEntity>>
}
