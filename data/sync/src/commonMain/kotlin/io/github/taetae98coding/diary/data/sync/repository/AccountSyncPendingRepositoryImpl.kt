package io.github.taetae98coding.diary.data.sync.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncPendingLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.sync.repository.AccountSyncPendingRepository
import kotlinx.coroutines.flow.Flow

@Inject
@ContributesBinding(AppScope::class)
internal class AccountSyncPendingRepositoryImpl(
    private val syncPendingLocalDataSource: SyncPendingLocalDataSource,
) : AccountSyncPendingRepository {
    override fun getPendingExists(account: Account): Flow<Boolean> = syncPendingLocalDataSource.getPendingExists(accountId = account.id)
}
