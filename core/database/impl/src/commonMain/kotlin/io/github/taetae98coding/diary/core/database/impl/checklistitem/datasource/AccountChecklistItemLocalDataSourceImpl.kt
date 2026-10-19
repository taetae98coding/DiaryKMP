package io.github.taetae98coding.diary.core.database.impl.checklistitem.datasource

import io.github.taetae98coding.diary.core.database.api.checklistitem.datasource.AccountChecklistItemLocalDataSource
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemSortLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
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
}
