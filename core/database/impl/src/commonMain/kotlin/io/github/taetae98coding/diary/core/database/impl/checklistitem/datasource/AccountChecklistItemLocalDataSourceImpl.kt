package io.github.taetae98coding.diary.core.database.impl.checklistitem.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.checklistitem.datasource.AccountChecklistItemLocalDataSource
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemSortLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountChecklistItemLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountChecklistItemLocalDataSource {
    override fun get(
        accountId: Uuid,
        checklistId: Uuid,
        sort: ChecklistItemSortLocalEntity,
    ): Flow<List<ChecklistItemLocalEntity>> =
        database.accountChecklistItemDao().get(
            accountId = accountId,
            checklistId = checklistId,
            sort = sort.queryValue,
        )

    override fun find(
        accountId: Uuid,
        itemId: Uuid,
    ): Flow<ChecklistItemLocalEntity?> =
        database.accountChecklistItemDao().find(
            accountId = accountId,
            itemId = itemId,
        )
}
