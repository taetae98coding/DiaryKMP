package io.github.taetae98coding.diary.core.database.api.sync.datasource

import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface SyncPendingLocalDataSource {
    public fun hasPending(accountId: Uuid): Flow<Boolean>
}
