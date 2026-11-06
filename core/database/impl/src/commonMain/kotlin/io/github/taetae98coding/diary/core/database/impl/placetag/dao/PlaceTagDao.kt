package io.github.taetae98coding.diary.core.database.impl.placetag.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.impl.entitytag.dao.EntityTagDao
import io.github.taetae98coding.diary.core.database.impl.placetag.entity.PlaceTagLocalEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface PlaceTagDao : EntityTagDao<PlaceTagLocalEntity> {
    @Query(
        """
        SELECT *
        FROM place_tag
        WHERE place_id IN (:entityIdList)
        """,
    )
    override suspend fun findByEntityIdList(entityIdList: List<Uuid>): List<PlaceTagLocalEntity>

    @Query(
        """
        UPDATE place_tag
        SET is_deleted = :isDeleted, updated_at = :updatedAt
        WHERE place_id = :entityId AND tag_id = :tagId
        """,
    )
    override suspend fun updateDeleted(
        entityId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
