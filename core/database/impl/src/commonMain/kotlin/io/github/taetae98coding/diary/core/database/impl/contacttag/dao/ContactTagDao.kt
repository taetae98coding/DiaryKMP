package io.github.taetae98coding.diary.core.database.impl.contacttag.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.impl.contacttag.entity.ContactTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entitytag.dao.EntityTagDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface ContactTagDao : EntityTagDao<ContactTagLocalEntity> {
    @Query(
        """
        SELECT *
        FROM contact_tag
        WHERE contact_id IN (:entityIdList)
        """,
    )
    override suspend fun findByEntityIdList(entityIdList: List<Uuid>): List<ContactTagLocalEntity>

    @Query(
        """
        UPDATE contact_tag
        SET is_deleted = :isDeleted, updated_at = :updatedAt
        WHERE contact_id = :entityId AND tag_id = :tagId
        """,
    )
    override suspend fun updateDeleted(
        entityId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
