package io.github.taetae98coding.diary.data.sync.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.datastore.api.sync.datasource.AccountSyncTimeLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.sync.repository.AccountSyncTimeRepository
import kotlin.time.Instant

@Inject
@ContributesBinding(AppScope::class)
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

    override suspend fun delete(account: Account) {
        accountSyncTimeLocalDataSource.delete(accountId = account.id)
    }
}
