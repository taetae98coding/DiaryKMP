package io.github.taetae98coding.diary.core.database.impl.memocontact.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.memocontact.datasource.AccountMemoContactSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memocontact.entity.MemoContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountMemoContactSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountMemoContactSyncLocalDataSource {
    override suspend fun readPendingList(accountId: Uuid): List<MemoContactLocalEntity> = database.accountMemoContactSyncDao().findPending(accountId = accountId)
}
