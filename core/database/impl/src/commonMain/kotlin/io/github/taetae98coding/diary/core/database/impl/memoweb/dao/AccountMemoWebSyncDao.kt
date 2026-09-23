package io.github.taetae98coding.diary.core.database.impl.memoweb.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.memoweb.entity.MemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.impl.memoweb.entity.AccountMemoWebLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface AccountMemoWebSyncDao : RoomDao<AccountMemoWebLocalEntity> {
    @Query(
        """
        SELECT memo_web.*
        FROM memo_web
        INNER JOIN account_memo_web
            ON account_memo_web.memo_id = memo_web.memo_id
                AND account_memo_web.web_id = memo_web.web_id
                AND account_memo_web.account_id = :accountId
        WHERE account_memo_web.is_dirty = 1
        """,
    )
    suspend fun findPending(accountId: Uuid): List<MemoWebLocalEntity>

    @Query(
        """
        UPDATE account_memo_web
        SET is_dirty = 0
        WHERE account_id = :accountId AND memo_id = :memoId AND web_id = :webId
            AND EXISTS(
                SELECT 1
                FROM memo_web
                WHERE memo_web.memo_id = :memoId AND memo_web.web_id = :webId AND memo_web.updated_at = :updatedAt
            )
        """,
    )
    suspend fun clearPending(
        accountId: Uuid,
        memoId: Uuid,
        webId: Uuid,
        updatedAt: Instant,
    ): Int
}
