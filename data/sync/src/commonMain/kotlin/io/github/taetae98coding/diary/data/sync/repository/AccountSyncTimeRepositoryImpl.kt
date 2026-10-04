package io.github.taetae98coding.diary.data.sync.repository

import io.github.taetae98coding.diary.core.datastore.api.sync.datasource.AccountSyncTimeLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.sync.repository.AccountSyncTimeRepository
import org.koin.core.annotation.Factory
import kotlin.time.Instant

@Factory
internal class AccountSyncTimeRepositoryImpl(
    private val accountSyncTimeLocalDataSource: AccountSyncTimeLocalDataSource,
) : AccountSyncTimeRepository {
    override suspend fun read(account: Account): Instant? = accountSyncTimeLocalDataSource.read(accountId = account.id)

    override suspend fun upsert(
        account: Account,
        syncedAt: Instant,
    ) {
        accountSyncTimeLocalDataSource.upsert(accountId = account.id, syncedAt = syncedAt)
    }
}
