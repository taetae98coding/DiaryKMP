package io.github.taetae98coding.diary.core.database.impl.checklist.datasource

import io.github.taetae98coding.diary.core.database.api.checklist.datasource.AccountChecklistSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountChecklistSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountChecklistSyncLocalDataSource {
    override suspend fun readPendingList(accountId: Uuid): List<ChecklistLocalEntity> = database.accountChecklistSyncDao().findPending(accountId = accountId)
}
