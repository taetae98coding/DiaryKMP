package io.github.taetae98coding.diary.core.database.impl.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface TagLinkDao : RoomDao<TagLinkLocalEntity> {
    @Query(
        """
        SELECT *
        FROM tag_link
        WHERE from_tag_id IN (:fromTagIdList)
        """,
    )
    suspend fun findByFromTagIdList(fromTagIdList: List<Uuid>): List<TagLinkLocalEntity>

    @Query(
        """
        UPDATE tag_link
        SET is_deleted = :isDeleted, updated_at = :updatedAt
        WHERE from_tag_id = :fromTagId AND to_tag_id = :toTagId
        """,
    )
    suspend fun updateDeleted(
        fromTagId: Uuid,
        toTagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
