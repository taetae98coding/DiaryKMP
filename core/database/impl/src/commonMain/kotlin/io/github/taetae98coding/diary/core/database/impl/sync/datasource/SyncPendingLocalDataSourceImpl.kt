package io.github.taetae98coding.diary.core.database.impl.sync.datasource

import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncPendingLocalDataSource
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
internal class SyncPendingLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : SyncPendingLocalDataSource {
    override fun hasPending(accountId: Uuid): Flow<Boolean> = database.syncPendingDao().hasPending(accountId = accountId)
}
