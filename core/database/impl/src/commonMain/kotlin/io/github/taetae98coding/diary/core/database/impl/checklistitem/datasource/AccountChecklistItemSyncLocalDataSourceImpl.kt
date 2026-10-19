package io.github.taetae98coding.diary.core.database.impl.checklistitem.datasource

import io.github.taetae98coding.diary.core.database.api.checklistitem.datasource.AccountChecklistItemSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountChecklistItemSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountChecklistItemSyncLocalDataSource {
    override suspend fun readPendingList(accountId: Uuid): List<ChecklistItemLocalEntity> = database.accountChecklistItemSyncDao().findPending(accountId = accountId)
}
