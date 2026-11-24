package io.github.taetae98coding.diary.core.database.impl.memotag.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.memotag.datasource.AccountMemoTagSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountMemoTagSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountMemoTagSyncLocalDataSource {
    override suspend fun readPendingList(accountId: Uuid): List<MemoTagLocalEntity> = database.accountMemoTagSyncDao().findPending(accountId = accountId)
}
