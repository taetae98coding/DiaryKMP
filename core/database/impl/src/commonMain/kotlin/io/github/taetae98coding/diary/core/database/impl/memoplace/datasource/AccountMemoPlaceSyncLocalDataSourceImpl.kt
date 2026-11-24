package io.github.taetae98coding.diary.core.database.impl.memoplace.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.memoplace.datasource.AccountMemoPlaceSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountMemoPlaceSyncLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : AccountMemoPlaceSyncLocalDataSource {
    override suspend fun readPendingList(accountId: Uuid): List<MemoPlaceLocalEntity> = database.accountMemoPlaceSyncDao().findPending(accountId = accountId)
}
