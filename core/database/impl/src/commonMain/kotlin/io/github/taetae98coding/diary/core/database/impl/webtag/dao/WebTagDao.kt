package io.github.taetae98coding.diary.core.database.impl.webtag.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.webtag.entity.WebTagLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface WebTagDao : RoomDao<WebTagLocalEntity> {
    @Query(
        """
        SELECT *
        FROM web_tag
        WHERE web_id IN (:webIdList)
        """,
    )
    suspend fun findByWebIdList(webIdList: List<Uuid>): List<WebTagLocalEntity>

    @Query(
        """
        UPDATE web_tag
        SET is_deleted = :isDeleted, updated_at = :updatedAt
        WHERE web_id = :webId AND tag_id = :tagId
        """,
    )
    suspend fun updateDeleted(
        webId: Uuid,
        tagId: Uuid,
        isDeleted: Boolean,
        updatedAt: Instant,
    ): Int
}
