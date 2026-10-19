package io.github.taetae98coding.diary.core.database.impl.checklist.datasource

import androidx.paging.PagingSource
import io.github.taetae98coding.diary.core.database.api.checklist.datasource.AccountChecklistLocalDataSource
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistSummaryLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountChecklistLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountChecklistLocalDataSource {
    override fun page(accountId: Uuid): PagingSource<Int, ChecklistSummaryLocalEntity> = database.accountChecklistDao().page(accountId = accountId)

    override fun find(
        accountId: Uuid,
        checklistId: Uuid,
    ): Flow<ChecklistLocalEntity?> =
        database.accountChecklistDao().find(
            accountId = accountId,
            checklistId = checklistId,
        )
}
