package io.github.taetae98coding.diary.core.database.impl.ddaytag.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.impl.ddaytag.entity.DDayTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entitytag.dao.EntityTagDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface DDayTagDao : EntityTagDao<DDayTagLocalEntity> {
    @Query(
        """
        SELECT *
        FROM dday_tag
        WHERE dday_id IN (:entityIdList)
        """,
    )
    override suspend fun findByEntityIdList(entityIdList: List<Uuid>): List<DDayTagLocalEntity>

    @Query(
        """
        UPDATE dday_tag
        SET is_deleted = :isDeleted, updated_at = :updatedAt
        WHERE dday_id = :entityId AND tag_id = :tagId
        """,
    )
    override suspend fun updateDeleted(
        entityId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
