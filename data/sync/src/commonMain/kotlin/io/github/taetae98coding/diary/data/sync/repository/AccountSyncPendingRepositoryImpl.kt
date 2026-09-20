package io.github.taetae98coding.diary.data.sync.repository

import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncPendingLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.sync.repository.AccountSyncPendingRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
internal class AccountSyncPendingRepositoryImpl(
    private val syncPendingLocalDataSource: SyncPendingLocalDataSource,
) : AccountSyncPendingRepository {
    override fun find(account: Account): Flow<Boolean> = syncPendingLocalDataSource.hasPending(accountId = account.id)
}
