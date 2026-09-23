package io.github.taetae98coding.diary.core.database.impl.sync.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.impl.sync.entity.SyncCursorLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.uuid.Uuid

@Dao
internal interface SyncCursorDao : RoomDao<SyncCursorLocalEntity> {
    @Query(
        """
        SELECT usn
        FROM sync_cursor
        WHERE account_id = :accountId AND kind = :kind
        """,
    )
    suspend fun find(
        accountId: Uuid,
        kind: String,
    ): Long?
}
