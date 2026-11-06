package io.github.taetae98coding.diary.core.database.impl.checklisttag.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.impl.checklisttag.entity.ChecklistTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entitytag.dao.EntityTagDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface ChecklistTagDao : EntityTagDao<ChecklistTagLocalEntity> {
    @Query(
        """
        SELECT *
        FROM checklist_tag
        WHERE checklist_id IN (:entityIdList)
        """,
    )
    override suspend fun findByEntityIdList(entityIdList: List<Uuid>): List<ChecklistTagLocalEntity>

    @Query(
        """
        UPDATE checklist_tag
        SET is_deleted = :isDeleted, updated_at = :updatedAt
        WHERE checklist_id = :entityId AND tag_id = :tagId
        """,
    )
    override suspend fun updateDeleted(
        entityId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
