package io.github.taetae98coding.diary.core.database.impl.datasource

import io.github.taetae98coding.diary.core.database.api.memotag.datasource.AccountMemoTagSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountMemoTagSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountMemoTagSyncLocalDataSource {
    override suspend fun findPending(accountId: Uuid): List<MemoTagLocalEntity> = database.accountMemoTagSyncDao().findPending(accountId = accountId)
}
