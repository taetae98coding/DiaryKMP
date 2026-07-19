package io.github.taetae98coding.diary.core.database.impl.datasource

import io.github.taetae98coding.diary.core.database.api.memo.datasource.AccountMemoSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountMemoSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountMemoSyncLocalDataSource {
    override suspend fun findPending(accountId: Uuid): List<MemoLocalEntity> = database.accountMemoSyncDao().findPending(accountId = accountId)
}
