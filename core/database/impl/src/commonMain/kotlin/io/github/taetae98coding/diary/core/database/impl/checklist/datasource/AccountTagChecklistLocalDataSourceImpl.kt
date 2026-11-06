package io.github.taetae98coding.diary.core.database.impl.checklist.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.checklist.datasource.AccountTagChecklistLocalDataSource
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistSummaryLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountTagChecklistLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountTagChecklistLocalDataSource {
    override fun page(
        accountId: Uuid,
        tagId: Uuid,
        scope: TagScopeLocalEntity,
    ): PagingSource<Int, ChecklistSummaryLocalEntity> =
        database.accountTagChecklistDao().page(
            accountId = accountId,
            tagId = tagId,
            scope = scope.queryValue,
        )
}
