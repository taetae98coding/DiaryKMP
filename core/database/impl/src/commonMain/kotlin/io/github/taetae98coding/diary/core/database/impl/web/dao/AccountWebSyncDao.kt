package io.github.taetae98coding.diary.core.database.impl.web.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.web.entity.AccountWebLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface AccountWebSyncDao : RoomDao<AccountWebLocalEntity> {
    @Query(
        """
        SELECT web.*
        FROM web
        INNER JOIN account_web
            ON account_web.web_id = web.id AND account_web.account_id = :accountId
        WHERE account_web.is_dirty = 1
        """,
    )
    suspend fun findPending(accountId: Uuid): List<WebLocalEntity>

    @Query(
        """
        UPDATE account_web
        SET is_dirty = 0
        WHERE account_id = :accountId AND web_id = :webId
            AND EXISTS(
                SELECT 1
                FROM web
                WHERE web.id = :webId AND web.updated_at = :updatedAt
            )
        """,
    )
    suspend fun clearPending(
        accountId: Uuid,
        webId: Uuid,
        updatedAt: Instant,
    ): Int
}
