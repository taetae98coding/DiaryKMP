package io.github.taetae98coding.diary.core.database.impl.tag.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.tag.entity.AccountTagLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface AccountTagSyncDao : RoomDao<AccountTagLocalEntity> {
    @Query(
        """
        SELECT tag.*
        FROM tag
        INNER JOIN account_tag
            ON account_tag.tag_id = tag.id AND account_tag.account_id = :accountId
        WHERE account_tag.is_dirty = 1
        """,
    )
    suspend fun findPending(accountId: Uuid): List<TagLocalEntity>

    @Query(
        """
        UPDATE account_tag
        SET is_dirty = 0
        WHERE account_id = :accountId AND tag_id = :tagId
            AND EXISTS(
                SELECT 1
                FROM tag
                WHERE tag.id = :tagId AND tag.updated_at = :updatedAt
            )
        """,
    )
    suspend fun clearPending(
        accountId: Uuid,
        tagId: Uuid,
        updatedAt: Instant,
    ): Int
}
