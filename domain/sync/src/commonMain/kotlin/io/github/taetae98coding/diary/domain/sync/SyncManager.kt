package io.github.taetae98coding.diary.domain.sync

import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

public interface SyncManager {
    public val isProgressReported: Flow<Boolean>

    public fun requestSync(
        accountId: Uuid,
        reportsProgress: Boolean,
    )
}
