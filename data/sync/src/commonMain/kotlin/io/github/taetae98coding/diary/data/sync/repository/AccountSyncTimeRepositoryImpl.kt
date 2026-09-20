package io.github.taetae98coding.diary.data.sync.repository

import io.github.taetae98coding.diary.core.datastore.api.sync.datasource.AccountSyncTimeLocalDataSource
import io.github.taetae98coding.diary.domain.sync.AccountSyncTimeRepository
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountSyncTimeRepositoryImpl(
    private val accountSyncTimeLocalDataSource: AccountSyncTimeLocalDataSource,
) : AccountSyncTimeRepository {
    override suspend fun find(accountId: Uuid): Instant? = accountSyncTimeLocalDataSource.find(accountId = accountId)

    override suspend fun upsert(
        accountId: Uuid,
        syncedAt: Instant,
    ) {
        accountSyncTimeLocalDataSource.upsert(accountId = accountId, syncedAt = syncedAt)
    }
}
