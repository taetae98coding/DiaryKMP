package io.github.taetae98coding.diary.core.database.impl.webtag.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.webtag.entity.WebTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.webtag.entity.AccountWebTagLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface AccountWebTagSyncDao : RoomDao<AccountWebTagLocalEntity> {
    @Query(
        """
        SELECT web_tag.*
        FROM web_tag
        INNER JOIN account_web_tag
            ON account_web_tag.web_id = web_tag.web_id
                AND account_web_tag.tag_id = web_tag.tag_id
                AND account_web_tag.account_id = :accountId
        WHERE account_web_tag.is_dirty = 1
        """,
    )
    suspend fun findPending(accountId: Uuid): List<WebTagLocalEntity>

    @Query(
        """
        UPDATE account_web_tag
        SET is_dirty = 0
        WHERE account_id = :accountId AND web_id = :webId AND tag_id = :tagId
            AND EXISTS(
                SELECT 1
                FROM web_tag
                WHERE web_tag.web_id = :webId AND web_tag.tag_id = :tagId AND web_tag.updated_at = :updatedAt
            )
        """,
    )
    suspend fun clearPending(
        accountId: Uuid,
        webId: Uuid,
        tagId: Uuid,
        updatedAt: Instant,
    ): Int
}
