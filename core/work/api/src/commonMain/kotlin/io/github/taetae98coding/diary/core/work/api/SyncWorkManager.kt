package io.github.taetae98coding.diary.core.work.api

import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface SyncWorkManager {
    public val state: Flow<SyncWorkState>

    public fun sync(accountId: Uuid)
}
