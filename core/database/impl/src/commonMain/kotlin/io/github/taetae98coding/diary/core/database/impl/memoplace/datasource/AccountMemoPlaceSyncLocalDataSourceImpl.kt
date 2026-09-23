package io.github.taetae98coding.diary.core.database.impl.memoplace.datasource

import io.github.taetae98coding.diary.core.database.api.memoplace.datasource.AccountMemoPlaceSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class AccountMemoPlaceSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountMemoPlaceSyncLocalDataSource {
    override suspend fun findPending(accountId: Uuid): List<MemoPlaceLocalEntity> = database.accountMemoPlaceSyncDao().findPending(accountId = accountId)
}
