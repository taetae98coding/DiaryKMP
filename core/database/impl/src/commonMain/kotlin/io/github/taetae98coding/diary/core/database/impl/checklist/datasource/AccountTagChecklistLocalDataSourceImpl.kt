package io.github.taetae98coding.diary.core.database.impl.checklist.datasource

import androidx.paging.PagingSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.checklist.datasource.AccountTagChecklistLocalDataSource
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistSummaryLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagScopeLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
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
