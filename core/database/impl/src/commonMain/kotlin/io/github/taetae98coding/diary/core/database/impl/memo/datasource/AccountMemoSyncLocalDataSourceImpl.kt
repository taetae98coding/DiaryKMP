package io.github.taetae98coding.diary.core.database.impl.memo.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.memo.datasource.AccountMemoSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountMemoSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountMemoSyncLocalDataSource {
    override suspend fun readPendingList(accountId: Uuid): List<MemoLocalEntity> = database.accountMemoSyncDao().findPending(accountId = accountId)
}
