package io.github.taetae98coding.diary.core.database.api.checklist.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistSummaryLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import kotlin.uuid.Uuid

public interface AccountTagChecklistLocalDataSource {
    public fun page(
        accountId: Uuid,
        tagId: Uuid,
        scope: TagScopeLocalEntity,
    ): PagingSource<Int, ChecklistSummaryLocalEntity>
}
