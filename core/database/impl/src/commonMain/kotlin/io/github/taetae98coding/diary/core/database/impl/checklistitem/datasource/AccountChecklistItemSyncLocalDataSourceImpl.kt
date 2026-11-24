package io.github.taetae98coding.diary.core.database.impl.checklistitem.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.checklistitem.datasource.AccountChecklistItemSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.checklistitem.entity.ChecklistItemLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountChecklistItemSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountChecklistItemSyncLocalDataSource {
    override suspend fun readPendingList(accountId: Uuid): List<ChecklistItemLocalEntity> = database.accountChecklistItemSyncDao().findPending(accountId = accountId)
}
