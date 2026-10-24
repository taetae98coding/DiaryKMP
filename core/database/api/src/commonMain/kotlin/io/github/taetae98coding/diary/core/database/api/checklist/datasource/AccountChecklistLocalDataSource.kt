package io.github.taetae98coding.diary.core.database.api.checklist.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistSummaryLocalEntity
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface AccountChecklistLocalDataSource {
    public fun page(accountId: Uuid): PagingSource<Int, ChecklistSummaryLocalEntity>

    public fun find(
        accountId: Uuid,
        checklistId: Uuid,
    ): Flow<ChecklistLocalEntity?>
}
