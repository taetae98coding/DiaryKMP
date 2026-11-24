package io.github.taetae98coding.diary.core.database.impl.sync.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncPendingLocalDataSource
import io.github.taetae98coding.diary.core.database.impl.DiaryDatabase
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class SyncPendingLocalDataSourceImpl(
    private val database: DiaryDatabase,
) : SyncPendingLocalDataSource {
    override fun getPendingExists(accountId: Uuid): Flow<Boolean> = database.syncPendingDao().hasPending(accountId = accountId)
}
