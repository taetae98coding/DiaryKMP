package io.github.taetae98coding.diary.core.database.impl.datasource

import io.github.taetae98coding.diary.core.database.api.memoweb.datasource.AccountMemoWebSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memoweb.entity.MemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountMemoWebSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountMemoWebSyncLocalDataSource {
    override suspend fun findPending(accountId: Uuid): List<MemoWebLocalEntity> = database.accountMemoWebSyncDao().findPending(accountId = accountId)
}
