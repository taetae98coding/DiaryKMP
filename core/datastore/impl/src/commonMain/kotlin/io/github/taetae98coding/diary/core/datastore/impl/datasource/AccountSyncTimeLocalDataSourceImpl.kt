package io.github.taetae98coding.diary.core.datastore.impl.datasource

import androidx.datastore.core.DataStore
import io.github.taetae98coding.diary.core.datastore.api.sync.datasource.AccountSyncTimeLocalDataSource
import io.github.taetae98coding.diary.core.datastore.impl.SyncTimeData
import io.github.taetae98coding.diary.core.datastore.impl.di.SyncTimeDataStore
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
internal class AccountSyncTimeLocalDataSourceImpl(
    @SyncTimeDataStore
    private val dataStore: DataStore<SyncTimeData>,
) : AccountSyncTimeLocalDataSource {
    override suspend fun find(accountId: Uuid): Instant? =
        dataStore
            .data
            .first()
            .syncedAtMap[accountId]

    override suspend fun upsert(
        accountId: Uuid,
        syncedAt: Instant,
    ) {
        dataStore.updateData { syncTime ->
            syncTime.copy(syncedAtMap = syncTime.syncedAtMap + (accountId to syncedAt))
        }
    }
}
