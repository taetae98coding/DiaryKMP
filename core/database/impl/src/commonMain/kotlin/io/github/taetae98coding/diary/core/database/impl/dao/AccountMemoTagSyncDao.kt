package io.github.taetae98coding.diary.core.database.impl.dao

import androidx.room3.Dao
import androidx.room3.Query
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.impl.entity.AccountMemoTagLocalEntity
import io.github.taetae98coding.diary.library.room3.dao.RoomDao
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
internal interface AccountMemoTagSyncDao : RoomDao<AccountMemoTagLocalEntity> {
    @Query(
        """
        SELECT memo_tag.*
        FROM memo_tag
        INNER JOIN account_memo_tag
            ON account_memo_tag.memo_id = memo_tag.memo_id
                AND account_memo_tag.tag_id = memo_tag.tag_id
                AND account_memo_tag.account_id = :accountId
        WHERE account_memo_tag.is_dirty = 1
        """,
    )
    suspend fun findPending(accountId: Uuid): List<MemoTagLocalEntity>

    @Query(
        """
        UPDATE account_memo_tag
        SET is_dirty = 0
        WHERE account_id = :accountId AND memo_id = :memoId AND tag_id = :tagId
            AND EXISTS(
                SELECT 1
                FROM memo_tag
                WHERE memo_tag.memo_id = :memoId AND memo_tag.tag_id = :tagId AND memo_tag.updated_at = :updatedAt
            )
        """,
    )
    suspend fun clearPending(
        accountId: Uuid,
        memoId: Uuid,
        tagId: Uuid,
        updatedAt: Instant,
    ): Int
}
