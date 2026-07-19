package io.github.taetae98coding.diary.core.database.impl.dao

import androidx.room3.Dao
import androidx.room3.MapColumn
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface MemoDao : RoomDao<MemoLocalEntity> {
    @Query(
        """
        SELECT id, updated_at
        FROM memo
        WHERE id IN (:idList)
        """,
    )
    suspend fun findUpdatedAt(idList: List<Uuid>): Map<
        @MapColumn("id")
        Uuid,
        @MapColumn("updated_at")
        Instant,
    >
}
