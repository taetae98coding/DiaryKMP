package io.github.taetae98coding.diary.core.database.impl.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.placetag.entity.PlaceTagLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface PlaceTagDao : RoomDao<PlaceTagLocalEntity> {
    @Query(
        """
        SELECT *
        FROM place_tag
        WHERE place_id IN (:placeIdList)
        """,
    )
    suspend fun findByPlaceIdList(placeIdList: List<Uuid>): List<PlaceTagLocalEntity>

    @Query(
        """
        UPDATE place_tag
        SET is_deleted = :isDeleted, updated_at = :updatedAt
        WHERE place_id = :placeId AND tag_id = :tagId
        """,
    )
    suspend fun updateDeleted(
        placeId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
