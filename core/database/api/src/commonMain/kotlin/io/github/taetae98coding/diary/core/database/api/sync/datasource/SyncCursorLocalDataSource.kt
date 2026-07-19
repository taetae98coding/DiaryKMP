package io.github.taetae98coding.diary.core.database.api.sync.datasource

import io.github.taetae98coding.diary.core.database.api.sync.SyncKind
import kotlin.uuid.Uuid

public interface SyncCursorLocalDataSource {
    public suspend fun find(
        accountId: Uuid,
        kind: SyncKind,
    ): Long

    public companion object {
        public const val DEFAULT_USN: Long = 0
    }
}
