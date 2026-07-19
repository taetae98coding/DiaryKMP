package io.github.taetae98coding.diary.core.database.impl.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountTagLinkLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface AccountTagLinkSyncDao : RoomDao<AccountTagLinkLocalEntity> {
    @Query(
        """
        SELECT tag_link.*
        FROM tag_link
        INNER JOIN account_tag_link
            ON account_tag_link.from_tag_id = tag_link.from_tag_id
                AND account_tag_link.to_tag_id = tag_link.to_tag_id
                AND account_tag_link.account_id = :accountId
        WHERE account_tag_link.is_dirty = 1
        """,
    )
    suspend fun findPending(accountId: Uuid): List<TagLinkLocalEntity>

    @Query(
        """
        UPDATE account_tag_link
        SET is_dirty = 0
        WHERE account_id = :accountId AND from_tag_id = :fromTagId AND to_tag_id = :toTagId
            AND EXISTS(
                SELECT 1
                FROM tag_link
                WHERE tag_link.from_tag_id = :fromTagId AND tag_link.to_tag_id = :toTagId AND tag_link.updated_at = :updatedAt
            )
        """,
    )
    suspend fun clearPending(
        accountId: Uuid,
        fromTagId: Uuid,
        toTagId: Uuid,
        updatedAt: Instant,
    ): Int
}
