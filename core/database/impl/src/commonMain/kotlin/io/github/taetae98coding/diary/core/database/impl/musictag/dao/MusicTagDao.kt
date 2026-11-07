package io.github.taetae98coding.diary.core.database.impl.musictag.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.impl.entitytag.dao.EntityTagDao
import io.github.taetae98coding.diary.core.database.impl.musictag.entity.MusicTagLocalEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface MusicTagDao : EntityTagDao<MusicTagLocalEntity> {
    @Query(
        """
        SELECT *
        FROM music_tag
        WHERE music_id IN (:entityIdList)
        """,
    )
    override suspend fun findByEntityIdList(entityIdList: List<Uuid>): List<MusicTagLocalEntity>

    @Query(
        """
        UPDATE music_tag
        SET is_deleted = :isDeleted, updated_at = :updatedAt
        WHERE music_id = :entityId AND tag_id = :tagId
        """,
    )
    override suspend fun updateDeleted(
        entityId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
