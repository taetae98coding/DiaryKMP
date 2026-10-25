package io.github.taetae98coding.diary.core.database.impl.checklist.dao

import androidx.room3.Dao
import androidx.room3.MapColumn
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.checklist.entity.ChecklistLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface ChecklistDao : RoomDao<ChecklistLocalEntity> {
    @Query(
        """
        SELECT id, updated_at
        FROM checklist
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
