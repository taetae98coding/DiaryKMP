package io.github.taetae98coding.diary.core.database.impl.memocontact.datasource

import io.github.taetae98coding.diary.core.database.api.memocontact.datasource.AccountMemoContactSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memocontact.entity.MemoContactLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountMemoContactSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountMemoContactSyncLocalDataSource {
    override suspend fun findPending(accountId: Uuid): List<MemoContactLocalEntity> = database.accountMemoContactSyncDao().findPending(accountId = accountId)
}
