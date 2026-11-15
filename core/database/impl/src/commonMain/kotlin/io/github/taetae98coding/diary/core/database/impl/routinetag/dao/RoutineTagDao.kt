package io.github.taetae98coding.diary.core.database.impl.routinetag.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.impl.entitytag.dao.EntityTagDao
import io.github.taetae98coding.diary.core.database.impl.routinetag.entity.RoutineTagLocalEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface RoutineTagDao : EntityTagDao<RoutineTagLocalEntity> {
    @Query(
        """
        SELECT *
        FROM routine_tag
        WHERE routine_id IN (:entityIdList)
        """,
    )
    override suspend fun findByEntityIdList(entityIdList: List<Uuid>): List<RoutineTagLocalEntity>

    @Query(
        """
        UPDATE routine_tag
        SET is_deleted = :isDeleted, updated_at = :updatedAt
        WHERE routine_id = :entityId AND tag_id = :tagId
        """,
    )
    override suspend fun updateDeleted(
        entityId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
