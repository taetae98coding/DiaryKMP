package io.github.taetae98coding.diary.core.datastore.impl.datasource

import androidx.datastore.core.DataStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.datastore.api.sync.datasource.AccountSyncTimeLocalDataSource
import io.github.taetae98coding.diary.core.datastore.impl.SyncTimeData
import io.github.taetae98coding.diary.core.datastore.impl.di.SyncTimeDataStore
import kotlinx.coroutines.flow.first
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Inject
@ContributesBinding(AppScope::class)
internal class AccountSyncTimeLocalDataSourceImpl(
    @SyncTimeDataStore
    private val dataStore: DataStore<SyncTimeData>,
) : AccountSyncTimeLocalDataSource {
    override suspend fun read(accountId: Uuid): Instant? =
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

    override suspend fun delete(accountId: Uuid) {
        dataStore.updateData { syncTime ->
            syncTime.copy(syncedAtMap = syncTime.syncedAtMap - accountId)
        }
    }
}
