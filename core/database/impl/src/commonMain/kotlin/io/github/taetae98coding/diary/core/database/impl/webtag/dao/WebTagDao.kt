package io.github.taetae98coding.diary.core.database.impl.webtag.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.impl.entitytag.dao.EntityTagDao
import io.github.taetae98coding.diary.core.database.impl.webtag.entity.WebTagLocalEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface WebTagDao : EntityTagDao<WebTagLocalEntity> {
    @Query(
        """
        SELECT *
        FROM web_tag
        WHERE web_id IN (:entityIdList)
        """,
    )
    override suspend fun findByEntityIdList(entityIdList: List<Uuid>): List<WebTagLocalEntity>

    @Query(
        """
        UPDATE web_tag
        SET is_deleted = :isDeleted, updated_at = :updatedAt
        WHERE web_id = :entityId AND tag_id = :tagId
        """,
    )
    override suspend fun updateDeleted(
        entityId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
