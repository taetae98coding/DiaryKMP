package io.github.taetae98coding.diary.core.database.impl.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMemoLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface AccountMemoSyncDao : RoomDao<AccountMemoLocalEntity> {
    @Query(
        """
        SELECT memo.*
        FROM memo
        INNER JOIN account_memo
            ON account_memo.memo_id = memo.id AND account_memo.account_id = :accountId
        WHERE account_memo.is_dirty = 1
        """,
    )
    suspend fun findPending(accountId: Uuid): List<MemoLocalEntity>

    @Query(
        """
        UPDATE account_memo
        SET is_dirty = 0
        WHERE account_id = :accountId AND memo_id = :memoId
            AND EXISTS(
                SELECT 1
                FROM memo
                WHERE memo.id = :memoId AND memo.updated_at = :updatedAt
            )
        """,
    )
    suspend fun clearPending(
        accountId: Uuid,
        memoId: Uuid,
        updatedAt: Instant,
    ): Int
}
