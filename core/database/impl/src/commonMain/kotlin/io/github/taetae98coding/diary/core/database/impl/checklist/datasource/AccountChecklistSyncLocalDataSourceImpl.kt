package io.github.taetae98coding.diary.core.database.impl.checklist.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.checklist.datasource.AccountChecklistSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountChecklistSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountChecklistSyncLocalDataSource {
    override suspend fun readPendingList(accountId: Uuid): List<ChecklistLocalEntity> = database.accountChecklistSyncDao().findPending(accountId = accountId)
}
