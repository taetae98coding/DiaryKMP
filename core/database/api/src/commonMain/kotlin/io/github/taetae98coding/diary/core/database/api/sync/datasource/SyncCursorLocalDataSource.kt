package io.github.taetae98coding.diary.core.database.api.sync.datasource

import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import kotlin.uuid.Uuid

public interface SyncCursorLocalDataSource {
    public suspend fun read(
        accountId: Uuid,
        kind: SyncKindLocalEntity,
    ): Long

    public companion object {
        public const val DEFAULT_USN: Long = 0
    }
}
