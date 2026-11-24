package io.github.taetae98coding.diary.core.database.impl.memoweb.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.memoweb.datasource.AccountMemoWebSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memoweb.entity.MemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountMemoWebSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountMemoWebSyncLocalDataSource {
    override suspend fun readPendingList(accountId: Uuid): List<MemoWebLocalEntity> = database.accountMemoWebSyncDao().findPending(accountId = accountId)
}
